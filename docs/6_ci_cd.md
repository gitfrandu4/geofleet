# CI/CD y Automatización

Este documento describe la configuración de CI/CD y las automatizaciones implementadas en el proyecto.

## Workflows de GitHub Actions

### 1. Android CI (`android-ci.yml`)

Este workflow se ejecuta en cada push y pull request para asegurar la calidad del código:

- **Triggers**: `push` y `pull_request` a `main`
- **Tareas**:
  - Configuración del entorno Android
  - Ejecución de ktlint
  - Análisis con Android Lint
  - Ejecución de pruebas unitarias
  - Subida de reportes como artefactos

### 2. AI Code Review (`ai-review.yml`)

Este workflow proporciona revisiones de código automáticas usando GPT-4:

- **Trigger**: Comentarios en PRs con comandos específicos
- **Comandos**:
  - `/review`: Solicita una revisión técnica detallada
  - `/summary`: Genera un resumen técnico educativo

#### Funcionamiento del AI Review

El workflow se activa mediante el evento `issue_comment`:

```yaml
on:
  issue_comment:
    types: [created]
```

Este evento se dispara cuando:
- Se crea un comentario en una issue o PR
- El comentario contiene los comandos `/review` o `/summary`
- El archivo del workflow existe en la rama por defecto

#### Permisos y Configuración

```yaml
permissions:
  contents: read
  pull-requests: write
  issues: read
```

#### Proceso de Revisión

1. **Obtención de cambios**:
   - Extrae detalles del PR usando la API de GitHub
   - Obtiene el diff completo del PR
   - Procesa y escapa caracteres especiales

2. **Análisis con GPT-4**:
   - Envía los cambios a OpenAI
   - Genera una revisión estructurada o resumen
   - Comenta los resultados en el PR

#### Ejemplos de Uso

Para solicitar una revisión en un PR:
```
/review
```

Para obtener un resumen técnico:
```
/summary
```

#### Estructura de las Revisiones

Las revisiones incluyen:
- Resumen ejecutivo
- Puntos destacados
- Áreas de atención
- Recomendaciones técnicas
- Checklist de verificación

#### Estructura de los Resúmenes

Los resúmenes técnicos incluyen:
- Análisis técnico
- Objetivos y soluciones
- Conceptos clave
- Lecciones y mejores prácticas
- Referencias y recursos

## Configuración Necesaria

1. **Secrets de GitHub**:
   - `OPENAI_API_KEY`: Para el AI Code Review
   - `MAPS_API_KEY`: Para los tests de integración
   - `GOOGLE_SERVICES_JSON`: Contenido del archivo google-services.json en base64
     ```bash
     # Generar el contenido del secret
     cat app/google-services.json | base64
     ```

2. **Permisos de Workflow**:
   - Habilitar "Read and write permissions"
   - Permitir "Allow GitHub Actions to create and approve pull requests"

### Configuración de Secrets

1. Ve a Settings > Secrets and variables > Actions en tu repositorio
2. Crea los siguientes secrets:

#### GOOGLE_SERVICES_JSON
Este secret debe contener el archivo google-services.json codificado en base64.

Para generarlo:
```bash
# En macOS/Linux:
base64 -i app/google-services.json

# En Windows (PowerShell):
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes("app/google-services.json"))
```

Verifica que:
- No hay saltos de línea en el contenido base64
- El contenido comienza con `ew` (que es `{` en base64)
- El archivo original es un JSON válido

#### Otros Secrets Requeridos
```
OPENAI_API_KEY=<tu-api-key-de-openai>
MAPS_API_KEY=<tu-api-key-de-google-maps>
```

### Verificación de la Configuración

1. Después de configurar los secrets, haz un push o crea un PR
2. Ve a la pestaña "Actions"
3. Verifica que el workflow muestre:
   - ✓ google-services.json created successfully
   - ✓ google-services.json is valid JSON

Si hay errores:
1. Verifica que el contenido base64 es correcto
2. Asegúrate de que el archivo JSON original es válido
3. Comprueba que no hay caracteres extra o saltos de línea en el secret

## Mejores Prácticas

1. **Revisiones de Código**:
   - Usar el comando `/review` para una primera revisión automática
   - Complementar con revisión manual del equipo
   - Considerar las sugerencias del AI reviewer

2. **Documentación**:
   - Usar `/summary` para generar documentación técnica
   - Mantener los resúmenes en la wiki del proyecto
   - Referenciar decisiones técnicas importantes 
