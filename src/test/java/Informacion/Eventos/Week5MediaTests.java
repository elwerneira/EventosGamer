package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Week5MediaTests {
    @Autowired MockMvc mvc;
    @Autowired AccountService accounts;
    @Autowired JwtService jwt;
    @Autowired EventMediaRepository media;

    private String bearer(int player) {
        var account = accounts.loadUserByUsername("player" + player + "@eventos.test");
        return "Bearer " + jwt.issue(new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities()));
    }

    private byte[] png() throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        return output.toByteArray();
    }

    @Test void ownerCanUploadAndAuthenticatedUsersCanReadTheExactImage() throws Exception {
        byte[] content = png();
        var file = new MockMultipartFile("file", "../../foto.png", "image/png", content);
        String body = mvc.perform(multipart("/api/eventos/1/media").file(file).with(csrf().asHeader())
                .header("Authorization", bearer(1)).param("caption", "Foto de prueba"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");
        assertThat(id).matches("[a-f0-9-]{36}");
        assertThat(media.findById(id).orElseThrow().content).isEqualTo(content);
        mvc.perform(get("/api/eventos/1/media/" + id).header("Authorization", bearer(2)))
            .andExpect(status().isOk()).andExpect(content().contentType("image/png"))
            .andExpect(header().string("Cache-Control", "no-store")).andExpect(content().bytes(content));
        mvc.perform(get("/api/eventos/1/media/" + id)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/eventos/2/media/" + id).header("Authorization", bearer(1)))
            .andExpect(status().isNotFound());
        mvc.perform(get("/eventos/1/media/" + id).with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().bytes(content));
    }

    @Test void uploadsNeedBothJwtAndCsrfAndCannotBeMadeByOtherUsers() throws Exception {
        var file = new MockMultipartFile("file", "foto.png", "image/png", png());
        mvc.perform(multipart("/api/eventos/1/media").file(file).with(csrf().asHeader()))
            .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/eventos/1/media").file(file).header("Authorization", bearer(1)))
            .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/eventos/1/media").file(file).with(csrf().asHeader())
                .header("Authorization", bearer(2)))
            .andExpect(status().isForbidden());
        assertThat(media.count()).isZero();
    }

    @Test void forgedImagesSvgEmptyFilesAndWrongMimeAreRejected() throws Exception {
        for (var file : new MockMultipartFile[]{
                new MockMultipartFile("file", "fake.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes()),
                new MockMultipartFile("file", "vector.svg", "image/svg+xml", "<svg/>".getBytes()),
                new MockMultipartFile("file", "empty.png", "image/png", new byte[0]),
                new MockMultipartFile("file", "wrong.jpg", "image/jpeg", png()),
                new MockMultipartFile("file", "unknown", null, png()),
                new MockMultipartFile("file", "fake.mp4", "video/mp4", png())}) {
            mvc.perform(multipart("/api/eventos/1/media").file(file).with(csrf().asHeader())
                    .header("Authorization", bearer(1))).andExpect(status().isBadRequest());
        }
        assertThat(media.count()).isZero();
    }

    @Test void oversizedFilesExcessivePixelsAndCaptionsAreRejectedWithoutPersistence() throws Exception {
        var oversized = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[16]) {
            @Override public long getSize() { return 26L * 1024 * 1024; }
        };
        mvc.perform(multipart("/api/eventos/1/media").file(oversized).with(csrf().asHeader())
            .header("Authorization", bearer(1))).andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/eventos/1/media").file(new MockMultipartFile("file", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1]))
            .with(csrf().asHeader()).header("Authorization", bearer(1))).andExpect(status().isBadRequest());
        byte[] hugePixels = png();
        ByteBuffer.wrap(hugePixels, 16, 8).putInt(10_000).putInt(10_000);
        var checksum = new CRC32(); checksum.update(hugePixels, 12, 17);
        ByteBuffer.wrap(hugePixels, 29, 4).putInt((int) checksum.getValue());
        mvc.perform(multipart("/api/eventos/1/media").file(new MockMultipartFile("file", "pixels.png", "image/png", hugePixels))
            .with(csrf().asHeader()).header("Authorization", bearer(1))).andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/eventos/1/media").file(new MockMultipartFile("file", "foto.png", "image/png", png()))
            .with(csrf().asHeader()).header("Authorization", bearer(1)).param("caption", "x".repeat(241)))
            .andExpect(status().isBadRequest());
        assertThat(media.count()).isZero();
    }

    @Test void mp4SignatureAndMetadataAreStoredWithoutReturningTheBlobInLists() throws Exception {
        byte[] mp4 = new byte[]{0,0,0,24,'f','t','y','p','i','s','o','m',0,0,0,0,'i','s','o','m','m','p','4','2'};
        mvc.perform(multipart("/api/eventos/1/media").file(new MockMultipartFile("file", "clip.mp4", "video/mp4", mp4))
            .with(csrf().asHeader()).header("Authorization", bearer(1)).param("caption", "Video de prueba"))
            .andExpect(status().isCreated());
        String result = mvc.perform(get("/api/eventos/1/comunidad").header("Authorization", bearer(2)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.media[0].kind").value("VIDEO"))
            .andReturn().getResponse().getContentAsString();
        assertThat(result).doesNotContain("\"content\":");
        mvc.perform(get("/eventos/1/comunidad").with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("<video")));
    }

    @Test void webUploadKeepsItsCsrfAndRendersEscapedCaptions() throws Exception {
        String caption = "<script>caption</script>";
        mvc.perform(multipart("/eventos/1/media").file(new MockMultipartFile("file", "foto.png", "image/png", png()))
                .with(csrf()).with(user("player1@eventos.test")).param("caption", caption))
            .andExpect(redirectedUrl("/eventos/1/comunidad"));
        mvc.perform(get("/eventos/1/comunidad").with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("&lt;script&gt;caption&lt;/script&gt;")))
            .andExpect(content().string(not(containsString(caption))));
    }

    @Test void uploadAndDownloadOfMissingEventOrMediaReturn404() throws Exception {
        mvc.perform(multipart("/api/eventos/999999/media").file(new MockMultipartFile("file", "foto.png", "image/png", png()))
            .with(csrf().asHeader()).header("Authorization", bearer(1))).andExpect(status().isNotFound());
        mvc.perform(get("/api/eventos/1/media/no-existe").header("Authorization", bearer(1)))
            .andExpect(status().isNotFound());
    }
}
