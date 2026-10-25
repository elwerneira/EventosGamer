# GREEGAME

## Ejecutar en Windows

Abre la carpeta `Eventos` en Visual Studio Code. Configura un JDK 17 o superior y verifica que `JAVA_HOME` apunte a su carpeta (no a `bin`). Se conservó Java 17 como versión de compilación. En la terminal de esa carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

Abre <http://localhost:8080/>. `/` muestra el inicio público de GREEGAME y `/home` se conserva como alias. El catálogo privado está en `/eventos`.

Maven descarga sus dependencias la primera vez. No requiere instalar Maven por separado. Si el puerto 8080 está ocupado, agrega `--server.port=8081` al comando `java -jar` y usa ese puerto en el navegador.

## Cuentas de demostración

| Correo | Contraseña inicial |
| --- | --- |
| player1@eventos.test | GamerDemo2026! |
| player2@eventos.test | GamerDemo2026! |
| player3@eventos.test | GamerDemo2026! |

Se crean únicamente cuando no existen. El registro permite crear cuentas adicionales con nombre, correo y contraseña. Las cuentas y sus perfiles persisten al reiniciar. Los usuarios de ejemplo se guardan también con BCrypt, no con contraseñas en texto plano en la base.

Antes del primer inicio puedes definir `EVENTOS_DEMO_PASSWORD` para usar otra contraseña en las tres cuentas de demostración. Cambiar esta variable después no modifica cuentas ya creadas. El registro exige entre 10 y 64 caracteres y un máximo de 72 bytes por el límite de BCrypt. El correo se normaliza a minúsculas y debe ser único.

## Rutas y controles

| Ruta | Acceso | Función |
| --- | --- | --- |
| `/`, `/home` | Público | Próximos eventos, destacados y publicidad ficticia |
| `/registro` | Público | Registro real de cuentas |
| `/login` | Público | Autenticación por correo y contraseña |
| `/login.css` | Público | Estilo adaptable de todas las vistas |
| `/perfil` | Privado | Nombre de usuario, juegos, avatar y preferencia de notificaciones |
| `/eventos` | Privado | Filtros combinables por categoría, fecha y ubicación |
| `/eventos/{id}` | Privado | Descripción, dirección, fecha, hora, organizadores, servicios y expositores |
| `/logout` (POST) | Sesión | Cierre de sesión con token CSRF |

Las demás rutas requieren autenticación. CSRF permanece activo para todos los formularios POST. Los perfiles se seleccionan a partir de la sesión, nunca a partir de un correo enviado desde el formulario. Las consultas usan parámetros y las vistas escapan el contenido de los usuarios. Las contraseñas no se devuelven en los formularios con errores.

## Referencias técnicas

- Spring Boot, requisitos: <https://docs.spring.io/spring-boot/system-requirements.html>
- Spring Security, CSRF: <https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html>
- Spring Boot, bases SQL: <https://docs.spring.io/spring-boot/reference/data/sql.html>
