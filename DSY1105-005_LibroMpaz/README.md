# Libro MPAZ — Evaluación Parcial 2 (DSY1105-005, Maipú)

App Android del caso de Vinculación con el Medio **"Libro Digital de Clases Interactivo 'Libro MPAZ' - Mpazbook"** de la Escuela Escritora Marcela Paz. La app reemplaza el libro de clases en papel con un registro digital por rol:

- **Docente:** registra la asistencia, los OA de cada clase, las notas y la hoja de vida.
- **UTP:** revisa la cobertura curricular y las calificaciones.
- **Inspectoría:** revisa la asistencia y las alertas.
- **Dirección:** ve las métricas del colegio y la auditoría.

## Integrantes

| Nombre | Usuario GitHub |
|---|---|
| [completar] | [completar] |
| [completar] | [completar] |

**Repositorio:** [completar enlace al repositorio de GitHub]
**Tablero Trello:** [completar enlace]

## Cómo ejecutar

1. Descomprimir el ZIP en una carpeta limpia.
2. En Android Studio, ir a **File > Open…** y elegir la carpeta que contiene `settings.gradle.kts`.
3. Esperar el Gradle Sync. La primera vez necesita internet.
4. Ejecutar ▶ **Run 'app'** en un emulador o teléfono con Android 7.0 (API 24) o superior.
5. Ingresar con uno de los usuarios de prueba.

Si Android Studio sugiere actualizar Gradle o el Android Gradle Plugin, **no aceptar**. Las versiones están fijadas por el curso.

La primera vez que se abre, la app carga en la base de datos local los datos de ejemplo de `data/MockData.kt`. Para volver a esos datos iniciales, hay que desinstalar la app o borrar sus datos.

Las pruebas unitarias de las validaciones se ejecutan con `./gradlew test`.

## Usuarios de prueba (datos ficticios)

| Perfil | Usuario | Clave |
|---|---|---|
| Docente (Matemática 3°A y 4°A) | docente.mat@libromp.cl | docente123 |
| Docente (Lenguaje 4°A) | docente.len@libromp.cl | docente123 |
| UTP | utp@libromp.cl | utp123 |
| Inspectoría | inspectoria@libromp.cl | inspectoria123 |
| Dirección | direccion@libromp.cl | direccion123 |

## Requerimientos implementados

| Requerimiento | Dónde se ve | Cómo se cumple |
|---|---|---|
| RF-01 Autenticar y mostrar solo las cargas propias | Login, Mis cursos | `login()` valida los campos y las credenciales contra la tabla `usuarios`. `misCargas()` filtra por el docente activo. |
| RF-02 RBAC por perfil | `AppNavigation` | Cada perfil entra a su propio panel. Cerrar sesión limpia toda la pila de navegación. |
| RF-03 Asistencia P/A/T/J | Asistencia | Se guarda en Room. Volver a guardar la misma fecha sobrescribe, previa confirmación. |
| RF-04 Asistencia en menos de un minuto | Asistencia | Todos parten como "presente": solo se marca a quien no lo está y se presiona Guardar. |
| RF-05 Inspectoría gestiona la asistencia | Panel Inspectoría | Ve el porcentaje por curso, puede abrir y corregir la asistencia de cualquier curso, y puede exportarla a CSV. |
| RF-06 OA y contenidos por sesión | Registro de clase | La sesión se guarda en la tabla `sesiones` y sus OA en la tabla intermedia `sesion_oa`. |
| RF-07 Cobertura curricular (UTP) | Cobertura curricular | Muestra los OA trabajados y los pendientes por curso y asignatura, con barra de avance. |
| RF-08 Notas con instrumento y fecha | Calificaciones | Se crea la evaluación y se registran notas de 1,0 a 7,0, con validación por campo. |
| RF-09 Desfases u omisiones (UTP) | Revisión de calificaciones | Detecta evaluaciones con notas faltantes y cargas docentes sin evaluaciones. |
| RF-10 Anotaciones con firma | Hoja de vida | La firma es el usuario activo más la fecha y hora automáticas. Una anotación no se puede modificar. |
| RF-11 Alertas de Inspectoría | Alertas | Muestra estudiantes con 3 o más ausencias y estudiantes con 2 o más anotaciones negativas. |
| RF-12 Estado global (Dirección) | Panel Dirección | Muestra por curso el % de asistencia, las clases, las notas y las anotaciones negativas. |
| RN-01 Sin acceso de estudiantes ni apoderados | Login | Solo existen los 4 perfiles del caso. |
| RN-02 Auditoría automática e inalterable | Auditoría | Cada creación o modificación guarda un `RegistroAuditoria` en la misma transacción de Room (`withTransaction`). El DAO de auditoría solo inserta y consulta. |
| RNF-02 Confirmar antes de sobrescribir | Asistencia, Calificaciones, Hoja de vida | Se muestra un `AlertDialog` antes de guardar. |
| RNF-03 Sin GPS, cámara, grabadora ni biometría | — | La app no usa ninguno de esos recursos. |
| RNF-04 Solo datos ficticios | `MockData.kt` | Los nombres son "Estudiante 3A-01", etc. No hay RUT ni datos reales. |

**Formularios con validación.** Login, Asistencia, Registro de clase, Nueva evaluación, Notas y Hoja de vida tienen validación:

- Cada campo muestra su error debajo, con ícono.
- Se confirma antes de sobrescribir.
- Se muestra un mensaje de éxito animado.
- Las reglas de validación están en `util/Validaciones.kt` y el ViewModel las expone. Así quedan fuera de las pantallas.

**Animaciones:**

- Transiciones entre pantallas: deslizar y aparecer.
- Color animado en los botones P/A/T/J.
- Mensajes de éxito y error que aparecen y desaparecen.
- Barras de porcentaje que se llenan al entrar.
- La lista de auditoría se anima.

## Recursos nativos

| Recurso | Dónde | Permiso | Por qué es pertinente al caso |
|---|---|---|---|
| **Vibración** (`nativo/Vibracion.kt`) | Al guardar: vibración corta si sale bien y doble si hay error. Aplica a asistencia, clase, notas, anotaciones, login y exportación. | `VIBRATE`, un permiso normal que Android concede al instalar sin preguntar. | El docente pasa la lista rápido (RF-04) mientras atiende al curso. La vibración confirma el guardado sin tener que leer la pantalla. |
| **Almacenamiento del teléfono** con el selector de archivos del sistema (Storage Access Framework, `nativo/ExportadorCsv.kt`) | Panel Inspectoría → "Exportar asistencia (CSV)" y Auditoría → "Exportar auditoría (CSV)". | No necesita permisos: el usuario elige la carpeta en el selector nativo. | Inspectoría y Dirección pueden respaldar y revisar en una planilla la asistencia y la auditoría (RF-05, RF-12). El archivo queda en el teléfono. |

Ninguno de los dos recursos está prohibido por el caso, que excluye GPS, cámara, grabadora y biometría. Ninguno envía información fuera del teléfono: la app no manda notificaciones, correos, SMS ni WhatsApp. Cada exportación queda registrada en la auditoría.

## Supuestos (pendientes de confirmar con el CITT)

- Las notas se validan en el rango **1,0 a 7,0**, con un decimal.
- Se puede **corregir la asistencia** de días anteriores. La corrección queda en la auditoría como `MODIFICAR`.
- La **firma digital** de una anotación es el registro automático del usuario más la fecha y hora. No se pide la clave otra vez.
- Los **OA** son un catálogo de ejemplo, no el catálogo oficial.
- **Porcentaje de asistencia:** se cuentan como asistencia "presente" y "atrasado"; "justificado" y "ausente" no cuentan.
- **Umbrales de alerta:** 3 o más ausencias, y 2 o más anotaciones negativas del mismo estudiante.
- Los roles son excluyentes: un usuario tiene un solo perfil.
- No se construye el backend en esta entrega. `data/remote/ApiPendiente.kt` queda para la Experiencia 3.

## Arquitectura (MVVM + Repository + Room)

```
Pantalla (Compose) ──collectAsState──> LibroMpazViewModel (StateFlow)
                                           │  validaciones: util/Validaciones.kt
                                           │  recursos nativos: nativo/
                                           ▼
                                   LibroMpazRepository (cambio + auditoría en una transacción)
                                           ▼
                                   DAOs ──> AppDatabase (Room / SQLite, libro_mpaz.db)
```

| Carpeta (`app/src/main/java/com/librompaz/app/`) | Contenido |
|---|---|
| `model/` | Entidades del caso. Las que son tablas llevan `@Entity`. |
| `data/local/` | `AppDatabase` (singleton, versión 1), `SesionEntity` y `SesionOa` (tabla intermedia). |
| `data/local/dao/` | `CatalogoDao`, `AsistenciaDao`, `SesionDao`, `CalificacionDao`, `AnotacionDao` y `AuditoriaDao` (solo insert y consultas). |
| `data/repository/` | `LibroMpazRepository`: lectura con `Flow` y escritura con `suspend` + `withTransaction`. |
| `data/MockData.kt` | Datos ficticios con que se llena la base la primera vez. |
| `viewmodel/` | `LibroMpazViewModel` (`AndroidViewModel`): estado con `StateFlow` y lógica de negocio. |
| `util/` | `Fechas.kt` y `Validaciones.kt` (funciones puras, probadas en `app/src/test`). |
| `nativo/` | `Vibracion.kt` y `ExportadorCsv.kt`. |
| `navigation/` | `AppNavigation`: rutas, ingreso por perfil, cierre de sesión y transiciones. |
| `ui/components/` | `BarraSuperior`, `SelectorDesplegable`, `CampoFecha` (DatePicker de Material 3), `Mensajes` y `BarraProgreso`. |
| `ui/screen/` | Pantallas por perfil: `docente/`, `utp/`, `inspectoria/`, `direccion/` y `comun/`. |

## Flujo de pantallas

```
LOGIN ──(según perfil)──┬─> DOCENTE: Mis cursos ──> Curso ──┬─> Asistencia del día
                        │                                   ├─> Registro de clase (OA y contenidos)
                        │                                   ├─> Calificaciones
                        │                                   └─> Hoja de vida
                        ├─> UTP: Panel ──> Cobertura curricular | Revisión de calificaciones
                        ├─> INSPECTORÍA: Panel ──> Asistencia (cualquier curso) | Alertas
                        └─> DIRECCIÓN: Panel ──> Auditoría
Cerrar sesión (cualquier perfil) ──> LOGIN
```

## Entrega

- **Nombre del ZIP:** `EP2_<sección>_<apellido1>_<apellido2>.zip`.
- **Contenido:** la carpeta que contiene `settings.gradle.kts`, **sin** las carpetas `build/`, `app/build/` ni `.gradle/`.
- **Antes de subir:** descomprimir el ZIP en otra carpeta y comprobar que el proyecto sincroniza, compila y ejecuta.
