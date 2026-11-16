# API Huerta Saludable

API REST para la aplicación Android Huerta Saludable, desarrollada con Java 17, Spring Boot 3, Spring Data JPA y MySQL. Android se comunica únicamente con esta API por HTTP; no debe conectarse directamente a MySQL.

## Requisitos

- JDK 17 o superior
- XAMPP con MySQL habilitado (o una instancia compatible)
- Wrapper Maven incluido (`mvnw.cmd` en Windows)

## Preparar MySQL con XAMPP

1. Inicia **MySQL** desde el panel de control de XAMPP.
2. En phpMyAdmin o en la consola MySQL, crea una base vacía:

   ```sql
   CREATE DATABASE huerta_saludable
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_unicode_ci;
   ```

3. Configura la conexión y los secretos en el entorno de la terminal desde la que ejecutarás la API. Ajusta el host, puerto, usuario y contraseña a la configuración local de MySQL; la API no presupone una contraseña:

   ```powershell
   $env:DB_HOST = "127.0.0.1"
   $env:DB_PORT = "3306"
   $env:DB_NAME = "huerta_saludable"
   $env:DB_USERNAME = "<usuario-mysql>"
   $env:DB_PASSWORD = "<contraseña-mysql>"
   ```

   `DB_URL` permite reemplazar la URL JDBC completa; si se omite, se compone con `DB_HOST`, `DB_PORT` y `DB_NAME`. `DB_USERNAME` y `DB_PASSWORD` deben configurarse explícitamente (si la instalación local usa una contraseña vacía, define `DB_PASSWORD` como una cadena vacía).

4. Genera un secreto JWT aleatorio de al menos 32 bytes y configura el primer administrador antes de iniciar la API:

   ```powershell
   $bytes = New-Object byte[] 32
   $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($bytes)
   $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
   $rng.Dispose()

   $env:JWT_EXPIRATION = "PT1H"
   $env:BOOTSTRAP_ADMIN_NAME = "<nombre-admin>"
   $env:BOOTSTRAP_ADMIN_EMAIL = "<correo-admin>"
   $env:BOOTSTRAP_ADMIN_PASSWORD = "<contraseña-de-10-a-72-caracteres>"
   ```

   `JWT_EXPIRATION` es una duración ISO-8601 positiva (`PT1H`, una hora, por defecto). `BOOTSTRAP_ADMIN_NAME`, `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` deben configurarse juntos. En el primer inicio se crea un administrador con contraseña BCrypt. Si ya existe un administrador, el bootstrap no crea ni modifica cuentas. Sin las variables de bootstrap la API puede iniciar, pero no habrá una cuenta con la que acceder hasta provisionar un administrador.

   En Windows, estas variables solo duran mientras vive la terminal actual. Para otro entorno usa su gestor de secretos o variables de entorno; no guardes contraseñas ni `JWT_SECRET` en el repositorio.

5. Inicia la API desde la raíz del proyecto:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   El puerto HTTP predeterminado es `8080`; se puede cambiar con `SERVER_PORT`. Para Android Emulator usa `http://10.0.2.2:8080`; desde un dispositivo físico usa la IP LAN del equipo que ejecuta la API y permite el tráfico por el firewall.

Hibernate actualiza el esquema automáticamente (`spring.jpa.hibernate.ddl-auto=update`) para facilitar el desarrollo local. No se incluyen migraciones de producción.

Al actualizar una base MySQL que ya fue creada con la versión anterior de la API, asegúrate de que las columnas opcionales permitan `NULL`:

```sql
ALTER TABLE tareas MODIFY sector_id BIGINT NULL;
ALTER TABLE reportes_problema MODIFY sector_id BIGINT NULL;
```

## Autenticación y permisos

Usa `Authorization: Bearer <accessToken>` en las rutas protegidas. La API firma JWT con HS256, valida expiración y rechaza cuentas inactivas al iniciar sesión. Las contraseñas se almacenan con BCrypt y nunca aparecen en respuestas.

| Método | Ruta | Acceso |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Público; solo crea `COLABORADOR` |
| `POST` | `/api/auth/login` | Público |
| `GET`, `PUT` | `/api/usuarios/me` | Usuario autenticado; solo su perfil, sin cambio de rol/estado |
| `GET`, `POST`, `PUT`, `DELETE` | `/api/usuarios[/{id}]` | `ADMIN` |
| `GET` | `/api/sectores[/{id}]` | Usuario autenticado |
| `POST`, `PUT`, `DELETE` | `/api/sectores[/{id}]` | `ADMIN` |
| `GET` | `/api/cultivos[/{id}]` | Usuario autenticado |
| `POST`, `PUT`, `DELETE` | `/api/cultivos[/{id}]` | `ADMIN` |
| `GET` | `/api/tareas[/{id}]` | `ADMIN` ve todas; `COLABORADOR` solo las asignadas a su cuenta |
| `POST`, `PUT`, `DELETE` | `/api/tareas[/{id}]` | `ADMIN` |
| `PATCH` | `/api/tareas/{id}/avance` | El colaborador asignado puede iniciar o enviar su tarea a validación |
| `GET` | `/api/reportes-problemas[/{id}]` | `ADMIN` ve todos; `COLABORADOR` solo los reportados por su cuenta |
| `POST` | `/api/reportes-problemas` | Usuario autenticado; el reportador se toma de la sesión |
| `PUT`, `DELETE` | `/api/reportes-problemas/{id}` | `ADMIN` |

El registro ignora cualquier intento de enviar un rol o estado administrativo. La gestión de usuarios (`POST /api/usuarios`) sí permite asignar roles, pero está protegida para `ADMIN`. `PUT /api/usuarios/me` no acepta rol ni estado; las operaciones de cada usuario se limitan a su propio perfil, tareas asignadas y reportes propios. Un colaborador puede modificar en sus tareas solo el estado `EN_DESARROLLO` o `PENDIENTE_VALIDACION`, su comentario y URI de evidencia.

Los enums aceptados son:

- Sector: `DISPONIBLE`, `EN_USO`, `EN_MANTENIMIENTO`, `FUERA_DE_USO`
- Cultivo: `PLANIFICADO`, `SEMBRADO`, `EN_CRECIMIENTO`, `LISTO_PARA_COSECHAR`, `COSECHADO`, `CON_PROBLEMAS`, `FINALIZADO`
- Tarea: `PENDIENTE`, `EN_DESARROLLO`, `PENDIENTE_VALIDACION`, `VALIDADA`, `RECHAZADA`, `ATRASADA`
- Reporte de problema: `INFORMADO`, `EN_REVISION`, `ATENDIDO`, `CERRADO`

Los IDs de responsable, sector, cultivo, estudiante, docente y reportador se validan contra la base de datos. `sectorId` es opcional en tareas y reportes; si se envía un `cultivoId` sin sector, la API deriva y devuelve el sector asignado al cultivo. Si se envían ambos IDs, el cultivo debe pertenecer al sector indicado. Si ambos se omiten, el recurso se guarda sin sector. Las respuestas de usuario no contienen `password`.

## Ejemplo de contrato

Registro de colaborador:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "nombre": "Camila Muñoz",
  "email": "camila@huerta.cl",
  "password": "una-clave-segura",
  "tallerGrupo": "Taller 5° Básico A"
}
```

La respuesta contiene `accessToken`, `tokenType`, `expiresIn` (segundos) y `usuario`. Enviar ese token en las siguientes llamadas:

```http
Authorization: Bearer <accessToken>
```

Crear un sector como administrador:

```json
{
  "codigo": "S-01",
  "nombre": "Invernadero",
  "descripcion": "Sector de cultivo protegido",
  "ubicacion": "Patio norte",
  "estado": "DISPONIBLE",
  "responsableId": null
}
```

Los recursos de sectores, cultivos, tareas y reportes ofrecen operaciones REST `GET` (colección y elemento), `POST`, `PUT /{id}` y `DELETE /{id}` de acuerdo con los permisos de la tabla. Los campos y estados reflejan los modelos Room indicados por la aplicación; `fechaSiembra` y `fechaEstimadaCosecha` de un cultivo son obligatorias, `fechaTrasplante` es opcional, y `fechaCreacion` del sector y `fechaReporte` del reporte los asigna el servidor. Las URI de fotografía/evidencia se conservan como texto, sin carga ni almacenamiento binario.

Las fechas se envían como cadenas JSON ISO-8601, no como epoch millis:

- `LocalDate`: `yyyy-MM-dd`, por ejemplo `"fechaProgramada": "2026-11-17"`.
- `LocalDateTime`: `yyyy-MM-dd'T'HH:mm:ss`, por ejemplo `"fechaValidacion": "2026-11-17T14:30:00"` (sin offset; los campos que genera la API usan UTC).

Android debe convertir sus epoch millis al formato correspondiente al enviar y volver a epoch millis al recibir, usando UTC de forma consistente. `LocalDate` representa una fecha de calendario; para convertirla desde epoch millis se toma la fecha UTC. `LocalDateTime` se interpreta como hora UTC aunque el JSON no lleve sufijo `Z`.

Errores comunes: `400` por validación o relaciones incompatibles, `401` por falta de token o credenciales inválidas, `403` por permisos insuficientes, `404` por recurso inexistente y `409` por correo/código duplicado o conflicto de integridad.

## Fuera de alcance

Las lecturas de sensores de la app son simuladas y no existe una fuente física ni contrato de ingestión fiable; permanecen en Android y no se exponen endpoints de sensores en esta fase. Tampoco se implementa un historial de actividad remoto: la app lo mantiene localmente hasta definir qué eventos del servidor deben auditarse y su retención. Las URI locales de imágenes de Android no son accesibles desde el servidor; se conservan en el contrato, pero no se pueden visualizar/sincronizar entre dispositivos hasta implementar carga autenticada de archivos.

## Pruebas

Las pruebas usan H2 en memoria y no requieren XAMPP:

```powershell
.\mvnw.cmd test
```
