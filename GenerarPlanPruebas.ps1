
$ErrorActionPreference = "Stop"
Get-Process -Name "WINWORD" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

$docPath = "C:\Users\Juan Loaiza\Desktop\Proyecto Gestion Residuos\GestionResiduosBack\Plan_Pruebas_GestionResiduos.docx"
if (Test-Path $docPath) { Remove-Item $docPath -Force }

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()
$sel = $word.Selection

function H1($t){ $sel.Font.Size=16;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.TypeParagraph() }
function H2($t){ $sel.Font.Size=13;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.TypeParagraph() }
function H3($t){ $sel.Font.Size=11;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.TypeParagraph() }
function P($t){  $sel.Font.Size=11;$sel.Font.Bold=$false;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.TypeParagraph() }
function PB($t){ $sel.Font.Size=11;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.Font.Bold=$false;$sel.TypeParagraph() }
function C($t){  $sel.Font.Size=9;$sel.Font.Bold=$false;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Courier New";$sel.TypeText($t);$sel.TypeParagraph();$sel.Font.Name="Calibri" }
function BR(){ $sel.Font.Name="Calibri";$sel.TypeParagraph() }
function PBK(){ $sel.InsertBreak([Microsoft.Office.Interop.Word.WdBreakType]::wdPageBreak) }
function Row($col1, $col2, $col3, $col4, $col5){
    $sel.Font.Size=10;$sel.Font.Bold=$false;$sel.Font.Name="Calibri"
    $sel.TypeText("  [$col1]  $col2  |  Entrada: $col3  |  Esperado: $col4  |  Estado: $col5")
    $sel.TypeParagraph()
}

# PORTADA
$sel.ParagraphFormat.Alignment=1
$sel.Font.Size=24;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Name="Calibri"
$sel.TypeText("Plan de Pruebas");$sel.TypeParagraph()
$sel.Font.Size=18;$sel.TypeText("Sistema de Gestion de Residuos");$sel.TypeParagraph()
$sel.Font.Size=12;$sel.Font.Bold=$false
$sel.TypeText("Version 1.0  -  Mayo 2026");$sel.TypeParagraph()
$sel.TypeParagraph()
$sel.Font.Size=11
$sel.TypeText("Cubre: Backend (Spring Boot) + Frontend (Angular)");$sel.TypeParagraph()
$sel.ParagraphFormat.Alignment=0
PBK

# 1. OBJETIVO
H1 "1. OBJETIVO DEL PLAN DE PRUEBAS"
P "Este documento define la estrategia, alcance, casos de prueba y criterios de aceptacion para validar el correcto funcionamiento del Sistema de Gestion de Residuos, tanto en su capa de backend (API REST en Spring Boot) como en su capa de frontend (Angular)."
BR
PB "Objetivos especificos:"
P "  - Verificar que todos los endpoints REST respondan correctamente segun su especificacion."
P "  - Verificar que los componentes del frontend consuman correctamente la API."
P "  - Validar el flujo completo de autenticacion, registro y gestion de usuarios."
P "  - Garantizar que las validaciones de negocio funcionen en ambas capas."
P "  - Detectar regresiones mediante pruebas automatizadas."
BR

# 2. ALCANCE
H1 "2. ALCANCE"
PB "Modulos cubiertos:"
P "  Backend: Auth, Usuarios, Ecopuntos, Reportes, Categorias, Calendario, Educacion, Quiz, Foro, Notificaciones, Ranking, Geografia."
P "  Frontend: Pantallas de login, registro, perfil, mapa de ecopuntos, reportes, contenido educativo, quiz, foro y notificaciones."
BR
PB "Fuera del alcance:"
P "  - Pruebas de carga y estres (performance avanzado)."
P "  - Pruebas de integracion con WhatsApp Business API y Telegram API (entornos externos)."
P "  - Pruebas de seguridad avanzada (pentesting)."
BR

# 3. TIPOS DE PRUEBA
H1 "3. TIPOS DE PRUEBA"
H2 "3.1 Pruebas Unitarias (Backend)"
P "Herramienta: JUnit 5 + Mockito + Spring Boot Test (@WebMvcTest)"
P "Ubicacion: src/test/java/..."
P "Cobertura: Todos los controladores REST del proyecto."
P "Ejecucion: mvn test"
BR
H2 "3.2 Pruebas de Integracion (Backend)"
P "Herramienta: @SpringBootTest + TestRestTemplate / MockMvc"
P "Objetivo: Verificar la comunicacion entre capas (Controller > Service > Repository > BD)."
P "Base de datos: H2 en memoria o PostgreSQL de prueba."
BR
H2 "3.3 Pruebas de API (Backend)"
P "Herramienta: Bruno (coleccion en docs/Gestion Residuos/)"
P "Objetivo: Probar cada endpoint manualmente con datos reales contra el servidor local."
P "URL base: http://localhost:8080"
BR
H2 "3.4 Pruebas de Componentes (Frontend)"
P "Herramienta: Jasmine + Karma (incluido en Angular)"
P "Objetivo: Verificar que cada componente Angular renderice correctamente y emita los eventos esperados."
P "Ejecucion: ng test"
BR
H2 "3.5 Pruebas E2E - Extremo a Extremo"
P "Herramienta: Cypress o Playwright"
P "Objetivo: Simular flujos completos de usuario (registro > login > crear reporte > ver ecopuntos)."
P "Ejecucion: npx cypress open"
BR
H2 "3.6 Pruebas de Regresion"
P "Ejecutar el conjunto completo de pruebas unitarias y de integracion antes de cada despliegue."
P "Ejecucion backend: mvn verify"
P "Ejecucion frontend: ng test --watch=false --code-coverage"
BR
PBK

# 4. CASOS DE PRUEBA BACKEND
H1 "4. CASOS DE PRUEBA - BACKEND"
BR

H2 "4.1 Autenticacion"
H3 "CP-AUTH-01: Registro exitoso de usuario ciudadano"
PB "Precondicion: El email no existe en la BD."
PB "Metodo: POST /auth/register/user"
PB "Entrada:"
C '{"documentNumber":123456,"names":"Juan","lastName":"Perez","email":"nuevo@correo.com","birthDate":"2000-01-15","neighborhoodId":1,"address":"Calle 1","password":"pass1234","phoneNumber":"3001234567"}'
PB "Resultado esperado: HTTP 200 con mensaje de verificacion."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-02: Login con credenciales validas"
PB "Precondicion: Usuario verificado y activo."
PB "Metodo: POST /auth/login"
PB "Entrada: {email:juan@correo.com, password:pass1234}"
PB "Resultado esperado: HTTP 200 con accessToken y refreshToken."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-03: Login con contrasena incorrecta"
PB "Metodo: POST /auth/login"
PB "Entrada: {email:juan@correo.com, password:incorrecta}"
PB "Resultado esperado: HTTP 401 con mensaje 'Credenciales invalidas'."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-04: Login con cuenta no verificada"
PB "Precondicion: Usuario registrado pero sin verificar email."
PB "Resultado esperado: HTTP 403 con mensaje de cuenta no verificada."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-05: Recuperacion de contrasena con email existente"
PB "Metodo: POST /auth/forgot-password  |  Entrada: {email:juan@correo.com}"
PB "Resultado esperado: HTTP 200 y correo enviado."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-06: Recuperacion de contrasena con email inexistente"
PB "Metodo: POST /auth/forgot-password  |  Entrada: {email:noexiste@correo.com}"
PB "Resultado esperado: HTTP 404."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-07: Renovar token con refreshToken valido"
PB "Metodo: POST /auth/refresh  |  Resultado esperado: HTTP 200 con nuevos tokens."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-08: Acceso a endpoint protegido sin token"
PB "Metodo: GET /api/users/profile (sin Authorization header)"
PB "Resultado esperado: HTTP 401."
PB "Estado: PENDIENTE"
BR
H3 "CP-AUTH-09: Acceso a endpoint de admin con rol ciudadano"
PB "Metodo: GET /api/users/admin/list (con JWT de CIUDADANO)"
PB "Resultado esperado: HTTP 403."
PB "Estado: PENDIENTE"
BR

H2 "4.2 Usuarios"
H3 "CP-USR-01: Obtener perfil del usuario autenticado"
PB "Metodo: GET /api/users/profile  |  Auth: JWT de ciudadano"
PB "Resultado esperado: HTTP 200 con datos del perfil incluyendo puntos y canUpdate."
PB "Estado: PENDIENTE"
BR
H3 "CP-USR-02: Actualizar perfil por primera vez en el mes"
PB "Metodo: PUT /api/users/profile  |  Form-data: names=NuevoNombre"
PB "Resultado esperado: HTTP 200 con canUpdate=false y nextUpdateAvailable con fecha."
PB "Estado: PENDIENTE"
BR
H3 "CP-USR-03: Intentar actualizar perfil dos veces en el mismo mes"
PB "Precondicion: Ya realizo una actualizacion este mes."
PB "Resultado esperado: HTTP 400 con mensaje de restriccion mensual."
PB "Estado: PENDIENTE"
BR
H3 "CP-USR-04: Cambiar estado de usuario a INACTIVO (admin)"
PB "Metodo: PATCH /api/users/admin/{documentNumber}/status  |  Body: {status:INACTIVO}"
PB "Resultado esperado: HTTP 200 con estado actualizado."
PB "Estado: PENDIENTE"
BR

H2 "4.3 Ecopuntos"
H3 "CP-ECO-01: Listar ecopuntos activos publicamente"
PB "Metodo: GET /api/ecopoints/active  |  Sin autenticacion"
PB "Resultado esperado: HTTP 200 con array (puede estar vacio)."
PB "Estado: PENDIENTE"
BR
H3 "CP-ECO-02: Crear ecopunto con coordenadas validas (admin)"
PB "Metodo: POST /api/ecopoints  |  Auth: JWT ADMINISTRADOR"
PB "Entrada: latitude=4.6097, longitude=-74.0817, name=Test, neighborhoodId=1"
PB "Resultado esperado: HTTP 201 con id asignado y status=ACTIVO."
PB "Estado: PENDIENTE"
BR
H3 "CP-ECO-03: Crear ecopunto con coordenadas invalidas"
PB "Entrada: latitude=200 (fuera de rango)"
PB "Resultado esperado: HTTP 400 con mensaje de validacion."
PB "Estado: PENDIENTE"
BR
H3 "CP-ECO-04: Obtener ecopunto por ID inexistente"
PB "Metodo: GET /api/ecopoints/9999"
PB "Resultado esperado: HTTP 404."
PB "Estado: PENDIENTE"
BR
H3 "CP-ECO-05: Filtrar ecopuntos por tipo de residuo PLASTICO"
PB "Metodo: GET /api/ecopoints/residue-type/PLASTICO"
PB "Resultado esperado: HTTP 200 con solo ecopuntos que aceptan PLASTICO."
PB "Estado: PENDIENTE"
BR

H2 "4.4 Reportes"
H3 "CP-REP-01: Crear reporte tipo punto_critico con imagen"
PB "Metodo: POST /api/reports  |  Form-data: type=punto_critico, latitude, longitude, image"
PB "Resultado esperado: HTTP 201 con imageUrl y status=pendiente."
PB "Estado: PENDIENTE"
BR
H3 "CP-REP-02: Crear reporte incumplimiento_calendario"
PB "Metodo: POST /api/reports  |  Form-data: type=incumplimiento_calendario, calendarId=1"
PB "Resultado esperado: HTTP 201 sin campos de coordenadas."
PB "Estado: PENDIENTE"
BR
H3 "CP-REP-03: Crear reporte punto_critico sin latitud"
PB "Entrada: type=punto_critico, sin latitude"
PB "Resultado esperado: HTTP 400 con mensaje de latitud obligatoria."
PB "Estado: PENDIENTE"
BR
H3 "CP-REP-04: Usuario sin reportes consulta mis reportes"
PB "Metodo: GET /api/reports/my-reports"
PB "Resultado esperado: HTTP 200 con array vacio []."
PB "Estado: PENDIENTE"
BR
H3 "CP-REP-05: Cambiar estado de reporte a resuelto"
PB "Metodo: PATCH /api/reports/1/status?newStatus=resuelto"
PB "Resultado esperado: HTTP 200 con resolvedAt con fecha."
PB "Estado: PENDIENTE"
BR
H3 "CP-REP-06: Busqueda paginada con filtros"
PB "Metodo: GET /api/reports/search?status=pendiente&page=0&size=5"
PB "Resultado esperado: HTTP 200 con estructura paginada (content, totalElements, totalPages)."
PB "Estado: PENDIENTE"
BR

H2 "4.5 Educacion"
H3 "CP-EDU-01: Listar contenidos sin autenticacion"
PB "Metodo: GET /api/v1/education  |  Sin token"
PB "Resultado esperado: HTTP 200 con lista de contenidos."
PB "Estado: PENDIENTE"
BR
H3 "CP-EDU-02: Crear contenido con archivos (admin)"
PB "Metodo: POST /api/v1/education  |  Form-data: title, description, category, files[]"
PB "Resultado esperado: HTTP 201 con archivos cargados."
PB "Estado: PENDIENTE"
BR
H3 "CP-EDU-03: Crear contenido sin archivos (admin)"
PB "Resultado esperado: HTTP 400 con mensaje de archivo requerido."
PB "Estado: PENDIENTE"
BR
H3 "CP-EDU-04: Ciudadano intenta crear contenido"
PB "Auth: JWT CIUDADANO  |  Resultado esperado: HTTP 403."
PB "Estado: PENDIENTE"
BR
H3 "CP-EDU-05: Dar feedback util por primera vez"
PB "Metodo: POST /api/v1/education/1/feedback  |  Body: {useful:true}"
PB "Resultado esperado: HTTP 200 con message='Feedback registrado'."
PB "Estado: PENDIENTE"
BR
H3 "CP-EDU-06: Dar feedback dos veces al mismo contenido"
PB "Resultado esperado: HTTP 200 con message='Ya se registro el feedback'."
PB "Estado: PENDIENTE"
BR

H2 "4.6 Quiz"
H3 "CP-QUIZ-01: Obtener quiz de un contenido sin respuestas correctas (usuario)"
PB "Metodo: GET /api/v1/quizzes/content/1"
PB "Resultado esperado: HTTP 200 sin campo correctIndex en preguntas."
PB "Estado: PENDIENTE"
BR
H3 "CP-QUIZ-02: Enviar respuestas del quiz"
PB "Metodo: POST /api/v1/quizzes/1/attempt"
PB "Resultado esperado: HTTP 200 con correctAnswers, pointsEarned, userTotalPoints."
PB "Estado: PENDIENTE"
BR
H3 "CP-QUIZ-03: Admin ve quiz con respuestas correctas"
PB "Metodo: GET /api/v1/quizzes/content/1/admin  |  Auth: JWT ADMINISTRADOR"
PB "Resultado esperado: HTTP 200 con correctIndex visible."
PB "Estado: PENDIENTE"
BR

H2 "4.7 Foro"
H3 "CP-FORO-01: Listar temas activos sin autenticacion"
PB "Metodo: GET /api/forum/topics"
PB "Resultado esperado: HTTP 200 con solo temas ACTIVO."
PB "Estado: PENDIENTE"
BR
H3 "CP-FORO-02: Crear tema autenticado"
PB "Metodo: POST /api/forum/topics  |  Body: {titulo, descripcion}"
PB "Resultado esperado: HTTP 201 con autorNombre del JWT."
PB "Estado: PENDIENTE"
BR
H3 "CP-FORO-03: Eliminar tema de otro usuario (no admin)"
PB "Resultado esperado: HTTP 403."
PB "Estado: PENDIENTE"
BR
H3 "CP-FORO-04: Agregar comentario a tema activo"
PB "Metodo: POST /api/forum/topics/1/comments  |  Body: {texto}"
PB "Resultado esperado: HTTP 201 con CommentDTO."
PB "Estado: PENDIENTE"
BR

H2 "4.8 Calendario"
H3 "CP-CAL-01: Consultar calendario por localidad"
PB "Metodo: GET /api/schedules/district/3  |  Sin autenticacion"
PB "Resultado esperado: HTTP 200 con horarios de esa localidad."
PB "Estado: PENDIENTE"
BR
H3 "CP-CAL-02: Crear horario (admin)"
PB "Metodo: POST /api/schedules  |  Body: {districtId, residueType, dayOfWeek, startTime, endTime}"
PB "Resultado esperado: HTTP 201 con status=ACTIVO."
PB "Estado: PENDIENTE"
BR
H3 "CP-CAL-03: Ciudadano intenta cambiar estado de horario"
PB "Metodo: PATCH /api/schedules/1/status  |  Auth: JWT CIUDADANO"
PB "Resultado esperado: HTTP 403."
PB "Estado: PENDIENTE"
BR

H2 "4.9 Notificaciones"
H3 "CP-NOTIF-01: Obtener preferencias del usuario autenticado"
PB "Metodo: GET /api/notifications/preferences"
PB "Resultado esperado: HTTP 200 con preferencias (si no existen, se crean con valores por defecto)."
PB "Estado: PENDIENTE"
BR
H3 "CP-NOTIF-02: Actualizar canales de notificacion"
PB "Metodo: PUT /api/notifications/preferences  |  Body: {emailEnabled:true, whatsappEnabled:true, whatsappNumber:3001234567}"
PB "Resultado esperado: HTTP 200 con preferencias actualizadas."
PB "Estado: PENDIENTE"
BR
H3 "CP-NOTIF-03: Listar campanas activas sin autenticacion"
PB "Metodo: GET /api/notifications/campaigns/active"
PB "Resultado esperado: HTTP 200 con campanas en estado ACTIVO."
PB "Estado: PENDIENTE"
BR
PBK

# 5. CASOS DE PRUEBA FRONTEND
H1 "5. CASOS DE PRUEBA - FRONTEND (Angular)"
BR

H2 "5.1 Pantalla de Login"
H3 "CP-FE-LOGIN-01: Login con credenciales validas"
PB "Pasos:"
P "  1. Ingresar a la pantalla de login."
P "  2. Ingresar email y contrasena validos."
P "  3. Hacer clic en 'Iniciar Sesion'."
PB "Resultado esperado: Redireccion al dashboard o pantalla principal. Token guardado en localStorage."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-LOGIN-02: Login con credenciales incorrectas"
PB "Pasos: Ingresar email valido con contrasena incorrecta."
PB "Resultado esperado: Mensaje de error visible 'Credenciales invalidas'. No redirige."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-LOGIN-03: Campos vacios al hacer login"
PB "Pasos: Hacer clic en 'Iniciar Sesion' sin llenar campos."
PB "Resultado esperado: Validaciones de formulario visibles en los campos."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-LOGIN-04: Enlace a recuperacion de contrasena"
PB "Pasos: Hacer clic en 'Olvide mi contrasena'."
PB "Resultado esperado: Navegar al formulario de recuperacion."
PB "Estado: PENDIENTE"
BR

H2 "5.2 Pantalla de Registro"
H3 "CP-FE-REG-01: Registro con todos los campos validos"
PB "Pasos:"
P "  1. Llenar todos los campos del formulario de registro."
P "  2. Seleccionar ciudad, localidad y barrio."
P "  3. Enviar formulario."
PB "Resultado esperado: Mensaje de exito. Instruccion de verificar correo."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-REG-02: Email ya registrado"
PB "Resultado esperado: Mensaje de error del servidor. Campo email marcado como invalido."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-REG-03: Carga de foto de perfil en el registro"
PB "Pasos: Seleccionar una imagen valida (jpg/png)."
PB "Resultado esperado: Preview de la imagen visible antes de enviar."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-REG-04: Cascada Ciudad > Localidad > Barrio"
PB "Pasos: Seleccionar ciudad. Verificar que se carguen las localidades. Seleccionar localidad. Verificar que se carguen los barrios."
PB "Resultado esperado: Cada selector se actualiza correctamente segun la seleccion anterior."
PB "Estado: PENDIENTE"
BR

H2 "5.3 Perfil de Usuario"
H3 "CP-FE-PERF-01: Ver datos del perfil"
PB "Precondicion: Usuario autenticado."
PB "Resultado esperado: Nombre, foto, email, barrio, puntos y estado de actualizacion visible."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-PERF-02: Editar perfil dentro del periodo permitido"
PB "Pasos: Ir a editar perfil. Cambiar nombre. Guardar."
PB "Resultado esperado: Datos actualizados. Campo canUpdate en false. Fecha proxima actualizacion visible."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-PERF-03: Boton editar deshabilitado cuando canUpdate=false"
PB "Precondicion: Usuario ya actualizo este mes."
PB "Resultado esperado: Boton de edicion deshabilitado o mensaje de restriccion."
PB "Estado: PENDIENTE"
BR

H2 "5.4 Mapa de Ecopuntos"
H3 "CP-FE-ECO-01: Cargar y mostrar ecopuntos en el mapa"
PB "Precondicion: Existen ecopuntos activos en la BD."
PB "Resultado esperado: Marcadores visibles en el mapa en las coordenadas correctas."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-ECO-02: Filtrar ecopuntos por tipo de residuo"
PB "Pasos: Seleccionar tipo PLASTICO en el filtro."
PB "Resultado esperado: Solo se muestran ecopuntos que aceptan PLASTICO."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-ECO-03: Ver detalle de ecopunto al hacer clic en marcador"
PB "Resultado esperado: Popup o panel con nombre, horario, tipos de residuo y direccion."
PB "Estado: PENDIENTE"
BR

H2 "5.5 Reportes Ciudadanos"
H3 "CP-FE-REP-01: Crear reporte de punto critico"
PB "Pasos:"
P "  1. Acceder a 'Nuevo Reporte'."
P "  2. Seleccionar tipo 'Punto Critico'."
P "  3. Seleccionar categoria."
P "  4. Ingresar descripcion."
P "  5. Seleccionar ubicacion en el mapa o GPS."
P "  6. Adjuntar imagen."
P "  7. Enviar."
PB "Resultado esperado: Reporte creado. Aparece en 'Mis Reportes'."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-REP-02: Crear reporte de incumplimiento de calendario"
PB "Pasos: Seleccionar tipo 'Incumplimiento Calendario'. Seleccionar el horario de la lista. Ingresar descripcion. Enviar."
PB "Resultado esperado: Reporte creado sin campos de coordenadas."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-REP-03: Listar mis reportes"
PB "Resultado esperado: Lista de reportes del usuario con estado, tipo y fecha."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-REP-04: Usuario sin reportes ve lista vacia"
PB "Resultado esperado: Mensaje indicando que no hay reportes registrados."
PB "Estado: PENDIENTE"
BR

H2 "5.6 Contenido Educativo"
H3 "CP-FE-EDU-01: Listar contenidos sin autenticacion"
PB "Resultado esperado: Contenidos visibles para cualquier visitante."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-EDU-02: Ver detalle de contenido con secciones"
PB "Pasos: Hacer clic en un contenido. Navegar por sus secciones."
PB "Resultado esperado: Titulo, descripcion, archivos (imagenes/PDF) y secciones con su propio contenido."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-EDU-03: Dar feedback 'Me fue util'"
PB "Pasos: Hacer clic en boton 'Me fue util'."
PB "Resultado esperado: Boton deshabilitado tras votar. Mensaje de confirmacion."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-EDU-04: Hacer quiz de un contenido"
PB "Pasos: Hacer clic en 'Iniciar Quiz'. Responder preguntas. Enviar."
PB "Resultado esperado: Resultado visible con puntaje obtenido y respuestas correctas."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-EDU-05: Admin crea nuevo contenido educativo"
PB "Pasos: Ir al panel admin. Crear contenido con titulo, descripcion, categoria y archivos."
PB "Resultado esperado: Contenido aparece en la lista publica."
PB "Estado: PENDIENTE"
BR

H2 "5.7 Foro Comunitario"
H3 "CP-FE-FORO-01: Ver lista de temas activos"
PB "Resultado esperado: Lista de temas con titulo, autor, fecha y cantidad de comentarios."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-FORO-02: Crear nuevo tema"
PB "Precondicion: Usuario autenticado."
PB "Pasos: Clic en 'Nuevo Tema'. Ingresar titulo y descripcion. Guardar."
PB "Resultado esperado: Tema aparece en la lista con el nombre del usuario como autor."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-FORO-03: Agregar comentario a un tema"
PB "Pasos: Abrir un tema. Escribir comentario. Enviar."
PB "Resultado esperado: Comentario visible en la lista con nombre del usuario y fecha."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-FORO-04: Responder a un comentario"
PB "Pasos: Hacer clic en 'Responder' en un comentario. Escribir texto. Enviar."
PB "Resultado esperado: Respuesta anidada visible bajo el comentario."
PB "Estado: PENDIENTE"
BR

H2 "5.8 Calendario de Recoleccion"
H3 "CP-FE-CAL-01: Ver calendario segun localidad del usuario"
PB "Resultado esperado: Dias y horarios de recoleccion para la localidad del usuario autenticado."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-CAL-02: Ver calendario de otra localidad"
PB "Pasos: Seleccionar una localidad diferente en el selector."
PB "Resultado esperado: Calendario actualizado con los horarios de la nueva localidad."
PB "Estado: PENDIENTE"
BR

H2 "5.9 Ranking"
H3 "CP-FE-RANK-01: Ver ranking de usuarios"
PB "Resultado esperado: Lista con posicion, nombre, foto y puntos. Ordenado de mayor a menor."
PB "Estado: PENDIENTE"
BR
H3 "CP-FE-RANK-02: Usuario autenticado ve su posicion destacada"
PB "Resultado esperado: La fila del usuario autenticado esta visualmente diferenciada."
PB "Estado: PENDIENTE"
BR
PBK

# 6. PRUEBAS E2E
H1 "6. FLUJOS E2E - EXTREMO A EXTREMO"
BR
H2 "FLUJO-01: Registro y Verificacion completa"
PB "Herramienta: Cypress"
P "  1. Ir a /registro."
P "  2. Llenar todos los campos correctamente."
P "  3. Enviar formulario."
P "  4. Verificar mensaje de exito."
P "  5. Simular clic en enlace de verificacion del correo."
P "  6. Verificar que el login ya funciona."
PB "Criterio de exito: Usuario puede iniciar sesion tras verificar."
BR
H2 "FLUJO-02: Login y creacion de reporte"
P "  1. Login con credenciales validas."
P "  2. Navegar a 'Nuevo Reporte'."
P "  3. Crear reporte tipo punto_critico con imagen y ubicacion."
P "  4. Verificar que aparece en 'Mis Reportes' con estado pendiente."
PB "Criterio de exito: Reporte visible con todos los datos."
BR
H2 "FLUJO-03: Admin gestiona ecopuntos"
P "  1. Login como ADMINISTRADOR."
P "  2. Crear un nuevo ecopunto."
P "  3. Verificar que aparece en el mapa."
P "  4. Editar el ecopunto."
P "  5. Cambiar estado a INACTIVO."
P "  6. Verificar que no aparece en la lista de activos."
PB "Criterio de exito: CRUD completo funcional en el panel admin."
BR
H2 "FLUJO-04: Completar quiz y ver puntos"
P "  1. Login como ciudadano."
P "  2. Ir a contenido educativo."
P "  3. Hacer el quiz."
P "  4. Verificar puntos obtenidos en el resultado."
P "  5. Verificar que los puntos se reflejan en el perfil."
P "  6. Verificar posicion en el ranking."
PB "Criterio de exito: Puntos actualizados en perfil y ranking."
BR
PBK

# 7. CONFIGURACION DE ENTORNO
H1 "7. CONFIGURACION DE ENTORNO DE PRUEBAS"
BR
H2 "Backend"
P "  Java: 21  |  Maven: 3.9+  |  Spring Boot: 3.5.x"
P "  Base de datos: PostgreSQL (pruebas) o H2 en memoria (pruebas unitarias)"
P "  Puerto: 8080"
P "  Ejecucion: mvn test (unitarias)  |  mvn verify (integracion)"
P "  Reporte: target/surefire-reports/"
BR
H2 "Frontend"
P "  Node: 18+  |  Angular CLI: 17+  |  npm: 9+"
P "  Ejecucion pruebas unitarias: ng test"
P "  Ejecucion con cobertura: ng test --watch=false --code-coverage"
P "  E2E: npx cypress open  (requiere backend corriendo)"
P "  Reporte de cobertura: coverage/lcov-report/index.html"
BR
H2 "Herramienta de API Manual"
P "  Bruno: Coleccion en docs/Gestion Residuos/"
P "  Variables de entorno: Configurar baseUrl=http://localhost:8080 y token=<JWT obtenido>"
BR
PBK

# 8. CRITERIOS DE ACEPTACION
H1 "8. CRITERIOS DE ACEPTACION"
BR
PB "Criterios para considerar una funcionalidad APROBADA:"
P "  - Todos los casos de prueba del modulo pasan sin errores."
P "  - Cobertura de codigo backend mayor al 70% en controladores y servicios."
P "  - Los flujos E2E principales (registro, login, reporte) pasan sin intervencion manual."
P "  - No existen errores 500 en el log durante las pruebas normales."
P "  - Las validaciones muestran mensajes claros al usuario final."
BR
PB "Criterios de RECHAZO:"
P "  - Cualquier endpoint critico (login, registro, reportes) retorna 500."
P "  - Un flujo E2E completo falla en mas del 20% de ejecuciones."
P "  - Se detecta brecha de seguridad (acceso sin token o con rol incorrecto)."
BR

# 9. RESPONSABLES
H1 "9. ROLES Y RESPONSABLES"
BR
P "  Backend Developer: Implementar y ejecutar pruebas unitarias e integracion (JUnit/Mockito)."
P "  Frontend Developer: Implementar pruebas de componentes (Jasmine/Karma) y E2E (Cypress)."
P "  QA / Tester: Ejecutar casos de prueba manuales con Bruno y registrar resultados."
P "  Tech Lead: Revisar cobertura, validar criterios de aceptacion y autorizar despliegue."
BR

# FOOTER
$sel.Font.Size=10;$sel.Font.Bold=$false;$sel.Font.Italic=$true;$sel.Font.Color=0;$sel.Font.Name="Calibri"
$sel.TypeText("Plan de Pruebas - Sistema de Gestion de Residuos - Version 1.0 - Mayo 2026")
$sel.TypeParagraph()

$doc.SaveAs2([ref]$docPath)
$word.Quit()
Write-Host "PLAN DE PRUEBAS GENERADO EN: $docPath"

