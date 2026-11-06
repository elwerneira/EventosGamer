# GREEGAME

GREEGAME reúne las funcionalidades del caso gamer y la revisión de seguridad de la sumativa: frontend Thymeleaf, API REST, Spring Security, persistencia JPA/MySQL y análisis OWASP Top 10 2021. El inicio incluye eventos recientes, populares y tres banners comerciales de demostración: VECTOR, RESPAWN+ y PIXEL LAB.

### Controles de seguridad incorporados

- Dos cadenas de Spring Security: sesión y CSRF para la web; JWT sin sesión para `/api/**`. El detalle y las fotografías son privados; `/api/admin/**` requiere ADMIN.
- Cabeceras CSP, Permissions Policy, Referrer Policy, CORP, COOP y COEP. La sesión web expira tras 15 minutos de inactividad y utiliza cookies HttpOnly y SameSite=Lax.
- Búsquedas con textos de hasta 254 caracteres y fechas entre 1900 y 2100; consultas JPA parametrizadas y salida escapada en Thymeleaf.
- Límite compartido entre logins web y API de 30 solicitudes por IP en 60 segundos, con respuesta 429 al superarlo. El control reside en memoria por instancia. Se registran autenticaciones sin contraseñas ni JWT.

### Resultados verificados

Revisión ampliada del **5 de noviembre de 2026** con ZAP 2.17.0 en modo pasivo, sobre copias aisladas anterior y corregida:

| Alcance | Altas | Medias | Bajas | Informativas |
| --- | ---: | ---: | ---: | ---: |
| Frontend antes | 0 | 1 | 4 | 4 |
| Frontend después | 0 | 0 | 0 | 4 |
| API antes | 0 | 0 | 1 | 3 |
| API después | 0 | 0 | 0 | 3 |

Se verificaron 29 solicitudes web y 23 API por versión: **104 solicitudes explícitas**, adicionales al rastreo. Incluyen inicio, registro por GET, siete filtros, detalles, fotografías y permisos sin token, USER y ADMIN. No se crearon cuentas ni se editaron perfiles o eventos durante estos análisis. Cada alcance conserva `report.html`, `report.json`, `coverage.json` y `configuration.json`.

Los avisos informativos se conservaron, incluido el potencial XSS de baja confianza. No se ejecutaron ataques activos. Estos resultados no certifican ausencia absoluta de vulnerabilidades; el informe relaciona los hallazgos y las pruebas con las diez categorías OWASP de la edición 2021.

### HTTPS local

Para iniciar MySQL y la aplicación con el perfil HTTPS, en una terminal dentro de `Eventos`:

```powershell
.\scripts\start-https.ps1
```

En otra terminal, comprobar la respuesta con el certificado esperado:

```powershell
curl.exe --cacert .\data\tls\greegame-local.pem https://localhost:8443/
```

El script conserva o genera el certificado de laboratorio bajo `data/tls`, configura TLS 1.2/1.3 y activa la cookie Secure. El certificado es autofirmado y no acredita confianza pública. Las claves privadas y contraseñas locales no se incluyen en Git ni en los paquetes de entrega.


## Cuentas de demostración

| Correo | Contraseña inicial |
| --- | --- |
| player1@eventos.test | GamerDemo2026! |
| player2@eventos.test | GamerDemo2026! |
| player3@eventos.test | GamerDemo2026! |

Se crean únicamente cuando no existen. El registro permite crear cuentas adicionales con nombre, correo y contraseña. Las cuentas y sus perfiles persisten al reiniciar. Los usuarios de ejemplo se guardan también con BCrypt, no con contraseñas en texto plano en la base.

El registro exige entre 10 y 64 caracteres y un máximo de 72 bytes por el límite de BCrypt. El correo se normaliza a minúsculas y debe ser único.

## Rutas y controles

| Ruta | Acceso | Función |
| --- | --- | --- |
| `/`, `/home` | Público | Próximos eventos, destacados y publicidad ficticia |
| `/registro` | Público | Registro real de cuentas |
| `/login` | Público | Autenticación por correo y contraseña |
| `/login.css` | Público | Estilo adaptable de todas las vistas |
| `/perfil` | Privado | Nombre de usuario, juegos, avatar y preferencia de notificaciones |
| `/eventos` (GET) | Público | Resúmenes y siete filtros combinables: nombre, videojuego, categoría, país, ciudad, fecha y modalidad |
| `/eventos/{id}` | Privado | Descripción, videojuego, ubicación o enlace, fecha, hora, organizadores, requisitos, inscripción, servicios y fotografías |
| `/eventos/fotos/{filename}` | Privado | Fotografías con sesión web; no se publican como recursos estáticos |
| `/logout` (POST) | Sesión | Cierre de sesión con token CSRF |

Las demás rutas requieren autenticación. CSRF permanece activo para todos los formularios POST. Los perfiles se seleccionan a partir de la sesión, nunca a partir de un correo enviado desde el formulario. Las consultas usan parámetros y las vistas escapan el contenido de los usuarios. Las contraseñas no se devuelven en los formularios con errores.

## Persistencia y conservación de datos

El perfil predeterminado es `mysql`. Las credenciales se leen desde variables de entorno; no se guardan contraseñas de MySQL en el código. `schema.sql` crea las tablas si faltan y `data.sql` inserta los eventos de ejemplo únicamente cuando no existen. Hibernate valida el esquema con `ddl-auto=validate`, sin borrar ni recrear tablas.

`Account` almacena la cuenta y su perfil en la tabla `accounts`; `StoredEvent` mapea la tabla `events`. `AccountRepository` y `EventJpaRepository` son repositorios Spring Data JPA. El registro fuerza una inserción y rechaza correos repetidos sin sobrescribir contraseñas. El perfil H2 se conserva como alternativa local y para compatibilidad con los datos anteriores:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=h2'
```

Los datos originales están en `data/eventos.mv.db`. No borrar ese archivo. En esta instalación se copiaron 3 cuentas con perfil y 3 eventos a MySQL, comparando todos sus campos y conservando los hashes BCrypt. La ampliación gamer agrega `event_details` y `event_photos`, sin modificar las columnas ni los valores de los eventos existentes. Se incorpora un cuarto evento online de demostración. La inicialización inserta solamente filas que faltan y conserva los datos editados. Las pruebas usan una base independiente; no deben ejecutarse contra `greegame`.

El inicio muestra los eventos próximos ordenados por fecha de publicación descendente y una selección de populares guardada en la base.

## Pruebas

```powershell
.\mvnw.cmd verify
```

Las pruebas habituales usan H2 en memoria con JPA. Para comprobar el motor MySQL, crear una base de verificación nueva, configurar `MYSQL_URL` con su nombre y ejecutar:

```powershell
.\mvnw.cmd '-Dspring.profiles.active=mysql' verify
```

El conjunto registra cuentas y edita perfiles de demostración. No reutilizar una base con datos de usuarios para esta comprobación.

## API JWT (Semana 2)

El arranque `scripts/start-local.ps1` prepara MySQL y carga `JWT_SECRET` desde `.env`. Si falta, genera una clave aleatoria de 32 bytes sin cambiar las contraseñas existentes. Mantener `.env` privado y conservarlo entre reinicios. Para ejecutar Maven directamente, cargar primero `scripts/start-mysql.ps1` en esa terminal; para H2 también se necesita `JWT_SECRET` en el entorno. La clave fija de `src/test/resources` es exclusivamente de pruebas.

| Ruta | Acceso |
| --- | --- |
| `POST /api/auth/login` | Público; JSON con `email` y `password` |
| `GET /api/eventos` | Público; resúmenes y los mismos siete filtros que la web |
| `GET /api/eventos/{id}` | Token JWT válido en `Authorization: Bearer TOKEN` |
| `GET /api/admin/resumen` | ADMIN; cantidad de cuentas y eventos |
| `GET /api/eventos/fotos/{filename}` | JWT válido; descarga de fotografías privadas |

Parámetros de búsqueda: `name`, `videoGame`, `category`, `country`, `location` (ciudad), `date` (`AAAA-MM-DD`) y `modality` (`Presencial` u `Online`). Son opcionales y se combinan. Nombre y videojuego admiten texto parcial sin distinguir mayúsculas. Los resultados públicos usan `EventSummary` y no contienen descripción, dirección, enlace de acceso, requisitos, inscripción ni fotografías. El detalle API incluye esos datos y los nombres de archivo de las fotografías; descargarlos mediante la ruta API protegida.

El login devuelve `accessToken`, `tokenType` y `expiresIn` (900 segundos). Las tres cuentas de demostración existentes pueden autenticarse usando su contraseña. La API consulta las cuentas y eventos mediante JPA. Valida firma HS256, emisor, vencimiento y existencia de la cuenta; consulta los permisos actuales de la cuenta al recibir cada token. Los roles se guardan en `account_roles`: player1 es ADMIN y muestra «Administrador · GREEGAME» en su perfil; player2 y player3 son USER.

La API `/api/**` usa autenticación Bearer sin sesión y no usa CSRF.
Ejemplo en PowerShell, después de iniciar la aplicación:

```powershell
$body = @{ email = 'player2@eventos.test'; password = 'GamerDemo2026!' } | ConvertTo-Json
$login = Invoke-RestMethod http://127.0.0.1:8080/api/auth/login -Method Post -ContentType 'application/json' -Body $body
Invoke-RestMethod http://127.0.0.1:8080/api/eventos/1 -Headers @{ Authorization = "Bearer $($login.accessToken)" }
```

JJWT: https://github.com/jwtk/jjwt#installation

- Spring Boot, requisitos: <https://docs.spring.io/spring-boot/system-requirements.html>
- Spring Security, CSRF: <https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html>
- Spring Boot, bases SQL: <https://docs.spring.io/spring-boot/reference/data/sql.html>
- Spring Boot, inicialización y validación del esquema: <https://docs.spring.io/spring-boot/how-to/data-initialization.html>
- MySQL, instalación mediante ZIP en Windows: <https://dev.mysql.com/doc/refman/8.4/en/windows-install-archive.html>
