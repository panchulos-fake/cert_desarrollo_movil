# PrestaAula – Préstamo de Recursos Tecnológicos y Material Didáctico

Aplicación móvil Android (Kotlin + Jetpack Compose + Material 3) para el **Colegio Marcela Paz**.
Reemplaza el cuaderno de papel del CRA por un registro digital de inventario, reservas, retiros y devoluciones.

> Asignatura: Desarrollo de Aplicaciones Móviles (DSY1105) · Evaluación Parcial 2 (Encargo)
> **Integrantes:** _Nombre Apellido_ y _Nombre Apellido_ · **Equipo N°:** _X_

## Problema que resuelve
- Dos docentes reservaban "mentalmente" el mismo proyector para el mismo bloque.
- Al devolver un equipo dañado o incompleto no quedaba un responsable claro.
- No existía un registro digital de disponibilidad ni de quién tiene cada equipo.

## Perfiles y funcionalidades
| Perfil | Qué puede hacer |
|---|---|
| **Docente** | Consultar disponibilidad por fecha y bloque, reservar sin choques, retirar/devolver, cancelar, registrar su disponibilidad horaria |
| **Encargado CRA** | Mantener inventario (alta/edición/estado), registrar retiros y devoluciones con estado y fotografía, ver reportes |
| **Jefe UTP / Jefe Administración y Finanzas** | Consultar préstamos, historial y reportes (uso e incidencias) |

Reglas de negocio implementadas:
- Un equipo no puede reservarse dos veces en el mismo bloque y fecha; un docente tampoco puede tener dos reservas en el mismo bloque.
- Equipos en mantención no se pueden reservar.
- Cada retiro/devolución actualiza el inventario en la misma transacción (disponible → prestado → disponible/mantención).
- Una devolución con daños exige observaciones y fotografía; una con faltantes exige observaciones. El equipo pasa a mantención y queda el responsable registrado.
- Historial completo por equipo con el último responsable.

## Cumplimiento de la rúbrica
| Indicador | Dónde se evidencia |
|---|---|
| Interfaz Material 3 y navegación | `ui/theme`, `ui/navigation/AppRoot.kt` (NavigationBar por rol, NavHost con transiciones) |
| Formularios validados con íconos y mensajes | `ui/components/FormComponents.kt`; formularios: login, registro, equipo, reserva, devolución, disponibilidad |
| Validaciones en la lógica (no en la UI) | `domain/validation/Validators.kt` usado desde los ViewModel |
| Gestión de estado | `viewmodel/*` con `StateFlow` + `collectAsStateWithLifecycle` |
| Animaciones | `AnimatedContent`, `AnimatedVisibility`, `animateColorAsState`, `animateFloatAsState`, `animateItem`, transiciones de navegación |
| Estructura modular + MVVM + persistencia | `data/` (Room, repositorios), `domain/`, `viewmodel/`, `ui/`, `di/` |
| Recursos nativos (2) | **Cámara** (`nativeaccess/CameraHelper.kt`, `ReturnScreen`) y **Notificaciones** (`nativeaccess/NotificationHelper.kt`) con permisos en tiempo de ejecución |

## Arquitectura
```
app/src/main/java/com/duoc/prestaaula/
├── data/        local (Room: entidades, DAOs, BD), repository, session, seed
├── domain/      model (Outcome, bloques horarios), validation (Validators)
├── nativeaccess/ CameraHelper, NotificationHelper
├── viewmodel/   un ViewModel por pantalla (estado inmutable + StateFlow)
├── ui/          theme, components, navigation, screens
├── di/          AppContainer (inyección manual)
└── util/        DateUtils, PasswordHasher
```
Patrón **MVVM**: Screen (Compose) → ViewModel → Repository → Room.

## Cómo ejecutar
1. Android Studio Koala (2024.1) o superior, JDK 17.
2. `File > Open` y seleccionar la carpeta del proyecto; esperar el *Gradle Sync*.
3. Emulador o dispositivo con **Android 8.0 (API 26)** o superior.
4. Ejecutar el módulo `app`.

### Cuentas de demostración
| Perfil | Correo | Contraseña |
|---|---|---|
| Encargado CRA | cra@marcelapaz.cl | Cra12345 |
| Docente | docente@marcelapaz.cl | Docente123 |
| Docente 2 | docente2@marcelapaz.cl | Docente123 |
| Jefe UTP | utp@marcelapaz.cl | Utp12345 |
| Jefe Administración | admin@marcelapaz.cl | Admin12345 |

### Flujo sugerido para probar
1. Ingresar como **docente** → Equipos → elegir un proyector → *Reservar* (fecha de hoy, bloque 1).
2. Intentar reservar el mismo equipo y bloque con **docente2**: la app muestra el choque.
3. Como **CRA** → Préstamos → abrir la reserva → *Registrar retiro*.
4. *Registrar devolución* → "Con daños" → observaciones + fotografía → el equipo pasa a mantención.
5. Como **UTP** → Reportes: incidencias con el último responsable.

## Seguridad y datos
- Datos solo en el dispositivo (Room/SQLite); contraseñas almacenadas como hash SHA-256.
- Fotografías en almacenamiento interno de la app, compartidas únicamente vía `FileProvider`.
- `allowBackup=false`.

## Alcance
Solo personal del establecimiento. No cubre compra de equipos ni acceso de apoderados/estudiantes.
