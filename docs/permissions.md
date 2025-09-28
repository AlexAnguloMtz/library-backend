### 📊 Matriz de permisos. Catálogo 'Usuarios'.

|                | **A sí mismo**                                                     | **Administrador**                                      | **Bibliotecario**                                       | **Usuario**                                            |
|----------------|--------------------------------------------------------------------|--------------------------------------------------------|---------------------------------------------------------|--------------------------------------------------------|
| **Administrador** | ✅ Leer<br> ✅ Editar<br> ❌ Borrar<br> .............................. | ✅ Leer<br> ❌ Editar<br> ❌ Borrar<br> ✅ Asignar este rol | ✅ Leer<br> ✅ Editar<br> ✅ Borrar<br> ✅ Asignar este rol | ✅ Leer<br> ✅ Editar<br> ✅ Borrar<br> ✅ Asignar este rol |
| **Bibliotecario** | ✅ Leer<br> ❌ Editar<br> ❌ Borrar<br> .............................. | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ✅ Leer<br> ✅ Editar<br> ✅ Borrar<br> ✅ Asignar este rol |
| **Usuario**       | ✅ Leer<br> ❌ Editar<br> ❌ Borrar<br> .............................. | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol |


### 📊 Matriz de permisos. Catálogo 'Autores'.

|                   | **Administrador**                            | **Bibliotecario**                                | **Usuario**                                      |
|-------------------|----------------------------------------------|--------------------------------------------------|--------------------------------------------------|
| **Autores**       | ✅ Leer<br> ✅ Crear<br> ✅ Editar<br> ✅ Borrar<br> | ✅ Leer<br> ✅ Crear<br> ✅ Editar<br> ✅ Borrar<br> | ✅ Leer<br> ❌ Crear<br> ❌ Editar<br> ❌ Borrar<br> |
