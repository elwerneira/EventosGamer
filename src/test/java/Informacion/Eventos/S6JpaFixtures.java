package Informacion.Eventos;
import java.time.LocalDateTime;
final class S6JpaFixtures {
 private S6JpaFixtures() {}
 static EventComment comment(String id,long event,String text,LocalDateTime time) {
  var c=new EventComment();c.id=id;c.eventId=event;c.authorEmail="player2@eventos.test";c.body=text;c.createdAt=time;return c;
 }
 static EventRating rating(String id,long event,int score) {
  var r=new EventRating();r.id=id;r.eventId=event;r.authorEmail="player2@eventos.test";r.score=score;return r;
 }
 static EventMedia media(String id,long event) {
  var m=new EventMedia();m.id=id;m.eventId=event;m.kind="VIDEO";m.contentType="video/mp4";
  m.caption="Clip técnico";m.createdAt=LocalDateTime.now();m.content=new byte[]{1,2,3};return m;
 }
}
