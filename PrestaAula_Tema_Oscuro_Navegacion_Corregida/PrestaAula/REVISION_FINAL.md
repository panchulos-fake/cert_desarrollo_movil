# Revisión final del proyecto PrestaAula (01-11-2026)

## Alcance de la revisión
Se revisaron los archivos Kotlin del proyecto, sus dependencias declaradas, el manifiesto Android y los flujos principales frente al caso DSY1105. Se hicieron cambios conservadores sin modificar el esquema Room (versión 1).

**Advertencia de verificación:** NO se ejecutó una compilación Android ni una prueba visual en emulador. En el entorno de análisis faltan Android SDK y la distribución Gradle 8.7; el intento de `bash gradlew --offline :app:assembleDebug --no-daemon` falla al tratar de descargar Gradle (`UnknownHostException: services.gradle.org`). Esta circunstancia NO demuestra un error de compilación en tu proyecto. Antes de la evaluación, abrir en Android Studio y ejecutar `Build > Make Project` y la demostración en un dispositivo/emulador.

## Correcciones aplicadas

| N.º | Hallazgo / riesgo | Modificación | Archivos |
|---|---|---|---|
| 1 | En un primer inicio las cuentas precargadas se insertaban en segundo plano, mientras la pantalla de inicio de sesión ya estaba activa. Podía aparecer un fallo temporal de autenticación. | Se agregó una señal `seedReady` en `AppContainer` y una pantalla de carga inicial hasta completar los datos. | `di/AppContainer.kt`, `ui/navigation/AppRoot.kt` |
| 2 | Un encargado CRA podía acceder al botón de reservar y crear una reserva a su propio nombre como si fuera docente. | Se deja crear reservas únicamente al perfil `DOCENTE`; se protege también el destino de navegación. | `ui/screens/equipment/EquipmentScreens.kt`, `ui/navigation/AppRoot.kt` |
| 3 | La pantalla de inventario podía declarar «Libre» un equipo físicamente prestado al consultar otro bloque del día actual. | Se señala como «Ocupado» si está prestado y se consulta la fecha actual o anterior, y el repositorio rechaza nuevas reservas de ese equipo para hoy. | `viewmodel/EquipmentListViewModel.kt`, `data/repository/LoanRepository.kt` |
| 4 | El repositorio de reservas no revalidaba el curso, bloque horario y máximo de 60 días (dependía de la pantalla). | Validación defensiva dentro de `reserve()`, además de la validación de UI. | `data/repository/LoanRepository.kt` |
| 5 | Las consultas asíncronas de disponibilidad podían completar en desorden al cambiar rápidamente fecha o bloque y mostrar un resultado para la selección anterior. | Se compara fecha+bloque antes de actualizar el resultado, se marca `checkingAvailability` y se desactiva confirmar hasta validar el bloque actual. | `viewmodel/ReservationViewModel.kt`, `ui/screens/reservation/ReservationScreen.kt` |
| 6 | El repositorio permitía que una devolución con daño se guardara sin observaciones o fotografía si otro consumidor omitía las validaciones de pantalla. | `returnLoan()` repite la comprobación de observaciones y de foto antes de persistir. | `data/repository/LoanRepository.kt` |
| 7 | El «último responsable» se elegía por fecha del bloque reservado, no necesariamente por el último retiro real. | Se elige el préstamo retirado/devuelto con mayor timestamp de `pickedUpAt`. | `viewmodel/EquipmentDetailViewModel.kt` |
| 8 | Pulsaciones múltiples rápidas podían lanzar más de una operación desde algunos ViewModels. | Guardas de `loading`, `saved` o `completed` en login, registro, reserva, devolución y formulario de equipos. | `viewmodel/{Login,Register,Reservation,Return,EquipmentForm}ViewModel.kt` |

## Verificaciones realizadas
- **16 pruebas JVM con `kotlinc` y `java -jar`: aprobadas**: validación de correo, contraseñas, códigos internos, cursos, fechas pasadas/futuras, observaciones, fotos obligatorias, rango de bloques, 8 bloques y conversión UTC.
- Revisión del manifiesto: se declaran `CAMERA`, `POST_NOTIFICATIONS` y `FileProvider`; recursos nativos presentes en el código.
- Inspección estática de flujos de navegación, repositorios, DAOs y ViewModels.
- **No se ha comprobado** arranque en Android, ejecución de cámara real, compatibilidad Gradle/Compose ni generación de APK.

## Límites de alcance importantes para la defensa
- La base Room/SQLite vive **en un solo dispositivo**: no sincroniza automáticamente inventario ni reservas entre teléfonos de docentes y CRA. Una fase posterior necesitaría backend/sincronización.
- La disponibilidad horaria personal puede registrarse en el perfil, pero **no se valida como una restricción de la reserva**. Sí se evitan choques de fecha+bloque por equipo y docente.
- Los correos y contraseñas de prueba figuran en la pantalla de login, deliberadamente por ser una demo. El hash SHA-256 sin salt no es una solución adecuada para autenticación de producción.
- La aplicación actual no gestiona compra de equipos ni tiene perfiles de alumnos o apoderados.
- El estado de un equipo en reparación se modifica manualmente por el CRA; no hay flujo separado de órdenes de mantenimiento.

## Prueba rápida antes de presentar (en Android Studio)
1. Abrir `PrestaAula` y sincronizar Gradle con acceso a internet y JDK 17. Luego `Build > Make Project`.
2. Borrar datos de la app solo **antes de preparar una demo nueva** y reiniciarla, para verificar los usuarios precargados. No borrar los datos durante la exposición.
3. Ingresar como `docente@marcelapaz.cl` / `Docente123`; reservar un recurso libre **para el día en que expones** (bloque 1, curso «5° Básico A»).
4. Cerrar sesión; entrar como `docente2@marcelapaz.cl` / `Docente123`, intentar reservar ese equipo y bloque para evidenciar el rechazo del choque. No confirmes una fecha/bloque distinto por error.
5. Cerrar sesión; ingresar como `cra@marcelapaz.cl` / `Cra12345`; pestaña Préstamos, reserva recién creada, `Registrar retiro` y `Registrar devolución`.
6. Elegir «Con daños», escribir «Pantalla trizada tras el uso» (más de 10 caracteres) y usar cámara; conceder permisos cuando corresponda. Confirmar y verificar mantención.
7. Ingresar como `utp@marcelapaz.cl` / `Utp12345` y mostrar la incidencia en Reportes.
8. Cerrar y volver a abrir la aplicación para comprobar que historial y estados permanecen en Room.

> Importante: `pickup()` no acepta préstamos con fecha futura. Para demostrar el ciclo completo en una sola sesión reserva para **hoy**. Si aparece un equipo ya reservado en el mismo bloque, elige otro que esté libre o cambia el bloque.
