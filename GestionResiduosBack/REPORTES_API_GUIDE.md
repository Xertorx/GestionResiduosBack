# 📋 API Reportes - HU10 Completo + Preparado para otras HUs

## ✅ Implementado - HU10 Reporte de Puntos Críticos

**Criticidad:** 5 - CRÍTICA

### Criterios de Aceptación ✅

- ✅ El reporte debe incluir foto, ubicación (GPS) y descripción
- ✅ El usuario debe recibir confirmación de que su reporte fue registrado
- ✅ El administrador debe visualizar el reporte en el panel de gestión

---

## 🏗️ Arquitectura

```
ReporteController (Endpoints)
    ↓
ReporteService (Lógica + Validaciones)
    ↓
ReporteRepository (Persistencia)
    ↓
Reporte Entity (Base de datos)
    
DTOs:
- ReporteCrearDTO (Entrada)
- ReporteDTO (Salida)
- EstadisticasReportesDTO (Stats)

Mapper:
- ReporteMapper (Conversiones Entity ↔ DTO)
```

---

## 🚀 ENDPOINTS

### 1️⃣ Crear Reporte (HU10 + HU11 + HU15)

**Endpoint:**
```
POST /api/reportes
Content-Type: multipart/form-data
Authorization: Bearer JWT_TOKEN
```

**Para HU10 - Punto Crítico:**
```bash
curl -X POST http://localhost:8080/api/reportes \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "tipo=punto_critico" \
  -F "categoriaId=1" \
  -F "descripcion=Mucha basura acumulada en la esquina" \
  -F "latitud=4.7110" \
  -F "longitud=-74.0721" \
  -F "imagen=@/ruta/foto.jpg"
```

**Respuesta (201 Created):**
```json
{
  "id": 1,
  "tipo": "punto_critico",
  "categoriaId": 1,
  "descripcion": "Mucha basura acumulada en la esquina",
  "latitud": 4.7110,
  "longitud": -74.0721,
  "imagenUrl": "/uploads/reportes/uuid_foto.jpg",
  "calendarioId": null,
  "estado": "pendiente",
  "usuarioId": 1,
  "usuarioNombre": "Juan Pérez",
  "createdAt": "2026-03-27",
  "updatedAt": "2026-03-27",
  "resueltoAt": null
}
```

**Validaciones HU10:**
- ❌ `latitud` NULL → Error 400
- ❌ `longitud` NULL → Error 400
- ❌ `imagen` NO enviada → Error 400
- ✅ `imagen` debe ser .jpg, .png, etc

---

### 2️⃣ Obtener Mis Reportes (HU34 - Ciudadano)

**Endpoint:**
```
GET /api/reportes/mis-reportes
Authorization: Bearer JWT_TOKEN
```

**Ejemplo:**
```bash
curl http://localhost:8080/api/reportes/mis-reportes \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "tipo": "punto_critico",
    "estado": "pendiente",
    "descripcion": "Mucha basura...",
    "createdAt": "2026-03-27"
  },
  {
    "id": 2,
    "tipo": "punto_critico",
    "estado": "en_revision",
    "descripcion": "Otra ubicación...",
    "createdAt": "2026-03-26"
  }
]
```

---

### 3️⃣ Obtener Todos los Reportes (HU25 - Admin)

**Endpoint:**
```
GET /api/reportes
Authorization: Bearer JWT_TOKEN (Admin)
```

```bash
curl http://localhost:8080/api/reportes \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

### 4️⃣ Obtener Reportes Pendientes (HU25 - Admin)

**Endpoint:**
```
GET /api/reportes/pendientes
```

```bash
curl http://localhost:8080/api/reportes/pendientes \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

### 5️⃣ Cambiar Estado (HU26 - Admin)

**Endpoint:**
```
PATCH /api/reportes/{id}/estado?nuevoEstado=ESTADO
Authorization: Bearer JWT_TOKEN (Admin)
```

**Estados válidos:** `pendiente`, `en_revision`, `resuelto`, `rechazado`

```bash
# Cambiar a en_revision
curl -X PATCH "http://localhost:8080/api/reportes/1/estado?nuevoEstado=en_revision" \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Cambiar a resuelto
curl -X PATCH "http://localhost:8080/api/reportes/1/estado?nuevoEstado=resuelto" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

**Respuesta:**
```json
{
  "id": 1,
  "estado": "resuelto",
  "resueltoAt": "2026-03-27",
  ...
}
```

---

### 6️⃣ Estadísticas (HU28 - Admin)

**Endpoint:**
```
GET /api/reportes/estadisticas
```

```bash
curl http://localhost:8080/api/reportes/estadisticas \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

**Respuesta:**
```json
{
  "total": 45,
  "pendientes": 12,
  "enRevision": 8,
  "resueltos": 20,
  "rechazados": 5,
  "puntosCriticos": 30,
  "incumplimientosCalendario": 15
}
```

---

### 7️⃣ Obtener Reporte por ID

**Endpoint:**
```
GET /api/reportes/{id}
```

```bash
curl http://localhost:8080/api/reportes/1
```

---

### 8️⃣ Filtrar por Tipo

**Endpoint:**
```
GET /api/reportes/tipo/{tipo}
```

```bash
# Obtener solo puntos críticos
curl http://localhost:8080/api/reportes/tipo/punto_critico

# Obtener solo incumplimientos
curl http://localhost:8080/api/reportes/tipo/incumplimiento_calendario
```

---

### 9️⃣ Filtrar por Estado

**Endpoint:**
```
GET /api/reportes/estado/{estado}
```

```bash
curl http://localhost:8080/api/reportes/estado/pendiente
curl http://localhost:8080/api/reportes/estado/resuelto
```

---

## 📝 Flujo Completo HU10

### Paso 1: Usuario se autentica
```bash
curl -X POST http://localhost:8080/auth/login \
  -d '{
    "email": "ciudadano@example.com",
    "password": "pass123"
  }'

# Guardar token
TOKEN="eyJhbGciOiJIUzI1NiIs..."
```

### Paso 2: Ciudadano toma foto y reporta punto crítico
```bash
curl -X POST http://localhost:8080/api/reportes \
  -H "Authorization: Bearer $TOKEN" \
  -F "tipo=punto_critico" \
  -F "categoriaId=1" \
  -F "descripcion=Hay basura acumulada en la calle 5" \
  -F "latitud=4.7110" \
  -F "longitud=-74.0721" \
  -F "imagen=@basura.jpg"
```

**Resultado:** ✅ Confirmación de reporte registrado

```json
{
  "id": 123,
  "estado": "pendiente",
  "mensaje": "Tu reporte ha sido registrado exitosamente"
}
```

### Paso 3: Ciudadano puede consultar su reporte
```bash
curl http://localhost:8080/api/reportes/mis-reportes \
  -H "Authorization: Bearer $TOKEN"
```

### Paso 4: Admin visualiza en panel
```bash
curl http://localhost:8080/api/reportes \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Paso 5: Admin cambia estado
```bash
curl -X PATCH "http://localhost:8080/api/reportes/123/estado?nuevoEstado=en_revision" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## 📊 Base de Datos

**Tabla: reportes**
```sql
CREATE TABLE reportes (
  id BIGSERIAL PRIMARY KEY,
  tipo VARCHAR(50) NOT NULL, -- punto_critico, incumplimiento_calendario
  categoria_id INTEGER NOT NULL,
  descripcion TEXT NOT NULL,
  latitud DOUBLE NOT NULL,
  longitud DOUBLE NOT NULL,
  imagen_url VARCHAR(255),
  calendario_id INTEGER,
  estado VARCHAR(50) DEFAULT 'pendiente', -- pendiente, en_revision, resuelto, rechazado
  usuario_id BIGINT NOT NULL,
  created_at DATE NOT NULL,
  updated_at DATE,
  resuelto_at DATE,
  FOREIGN KEY (usuario_id) REFERENCES users(id)
);
```

---

## 🔄 Preparado para Futuras HUs

### HU11 - Clasificación de Reportes
✅ Campo `categoriaId` ya soportado

### HU15 - Incumplimiento en Calendario
✅ Validaciones condicionales: `tipo=incumplimiento_calendario` + `calendarioId`

### HU25 - Visualización de Reportes
✅ `GET /api/reportes` (admin)

### HU26 - Gestión de Estado
✅ `PATCH /api/reportes/{id}/estado`

### HU27 - Notificación al Ciudadano
⏳ Próxima: Agregar EventListener para cambios de estado → enviar email/SMS

### HU28 - Estadísticas
✅ `GET /api/reportes/estadisticas`

### HU34 - Consulta por Ciudadano
✅ `GET /api/reportes/mis-reportes`

---

## ⚙️ Configuración

### En `application.properties`:
```properties
# Directorio de subida de imágenes
app.upload.dir=./uploads/reportes

# Tamaño máximo de archivo
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

---

## ✅ Validaciones Implementadas

| Campo | Validación | Tipo |
|-------|-----------|------|
| `tipo` | Debe ser punto_critico o incumplimiento_calendario | Obligatorio |
| `categoriaId` | No nulo | Obligatorio |
| `descripcion` | No nulo | Obligatorio |
| `latitud` | Obligatorio si tipo=punto_critico | Condicional |
| `longitud` | Obligatorio si tipo=punto_critico | Condicional |
| `imagen` | Obligatorio si tipo=punto_critico | Condicional |
| `calendarioId` | Obligatorio si tipo=incumplimiento_calendario | Condicional |

---

## 🔐 Seguridad

- ✅ JWT requerido para crear reportes
- ✅ Usuario solo ve sus propios reportes
- ✅ Admin acceso a todos
- ✅ Validación de archivos de imagen
- ✅ Almacenamiento seguro en servidor

---

**¡Sistema listo para HU10 y extensible para las demás!** 🚀

