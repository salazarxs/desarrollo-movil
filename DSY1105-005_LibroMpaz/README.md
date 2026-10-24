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

La app usa los datos de ejemplo de `data/MockData.kt`.

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
| RF-01 Iniciar sesión | Login | `login()` revisa que los campos no estén vacíos y compara el correo y la clave con `MockData.usuarios`. |
| RF-05 Inspectoría consulta la asistencia general | Panel Inspectoría | `porcentajeAsistencia()` calcula el % de asistencia de cada curso. Desde el panel se puede abrir la asistencia de cualquier curso. |

**Fórmula del RF-05:** (presentes + atrasados) / total de registros del curso × 100, redondeado. "Justificado" y "ausente" no cuentan como asistencia. Si el curso no tiene registros, se muestra "—".

Con los datos de prueba (RA1 a RA3 de 3° Básico A: P, A, T), el panel muestra "Asistencia: 67 %" para 3° Básico A y "Asistencia: —" para 4° Básico A.

## Supuestos (pendientes de confirmar con el CITT)

- Las notas se validan en el rango **1,0 a 7,0**, con un decimal.
- Se puede **corregir la asistencia** de días anteriores. La corrección queda en la auditoría como `MODIFICAR`.
- La **firma digital** de una anotación es el registro automático del usuario más la fecha y hora. No se pide la clave otra vez.
- Los **OA** son un catálogo de ejemplo, no el catálogo oficial.
- **Umbrales de alerta:** 3 o más ausencias, y 2 o más anotaciones negativas del mismo estudiante.
- Los roles son excluyentes: un usuario tiene un solo perfil.
- No se construye el backend en esta entrega.

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
