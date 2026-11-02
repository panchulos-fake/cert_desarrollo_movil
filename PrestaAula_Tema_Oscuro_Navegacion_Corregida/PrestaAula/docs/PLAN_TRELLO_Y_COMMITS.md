# Plan de trabajo (Trello) y guía de commits

## Tablero Trello sugerido
Listas: **Backlog · En progreso · En revisión · Hecho**. Una tarjeta por ítem, con responsable (Estudiante A / B) y checklist.

| # | Tarjeta | Resp. |
|---|---|---|
| 1 | Levantar proyecto Gradle + tema Material 3 | A |
| 2 | Modelo de datos Room (entidades, DAOs, BD) | B |
| 3 | Repositorios y reglas de negocio (reserva sin choques, retiro, devolución) | B |
| 4 | Validators centralizados | A |
| 5 | Login y registro de docentes | A |
| 6 | Listado de equipos con filtros (fecha/bloque/búsqueda) | A |
| 7 | Detalle e historial de equipo + formulario de inventario | B |
| 8 | Formulario de reserva | A |
| 9 | Préstamos: listado, detalle, retiro, cancelación | B |
| 10 | Devolución con cámara (recurso nativo 1) | B |
| 11 | Notificaciones locales (recurso nativo 2) | A |
| 12 | Reportes de uso e incidencias | B |
| 13 | Perfil y disponibilidad docente | A |
| 14 | Animaciones y transiciones | A |
| 15 | README, pruebas manuales y entrega AVA | A+B |

## Estrategia Git
- Rama `main` protegida; ramas `feature/<tema>`; Pull Request revisado por el otro integrante.
- Cada integrante hace sus propios commits desde su cuenta (la rúbrica evalúa participación distribuida).
- **No hacer ningún commit después de la fecha de entrega** (penalización automática).

### Ejemplos de mensajes técnicos
```
feat(data): agrega entidades y DAOs de Room para equipos y préstamos
feat(domain): centraliza validaciones de formularios en Validators
feat(loans): impide reservas duplicadas por equipo, fecha y bloque
feat(ui): agrega ValidatedTextField con ícono de estado y mensaje de error
feat(native): integra cámara con FileProvider para evidencia de devolución
feat(native): agrega notificaciones locales con permiso POST_NOTIFICATIONS
feat(nav): implementa NavigationBar por rol y transiciones animadas
fix(loans): bloquea el retiro si el equipo sigue prestado
docs: agrega README con pasos de ejecución y cuentas de prueba
```
