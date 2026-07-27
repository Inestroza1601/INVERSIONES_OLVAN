<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="margin-bottom: 20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:15px;">
        <div>
            <h2><i class="fas fa-user-cog" style="color:var(--primary);"></i> Control de Usuarios y Roles</h2>
            <p style="color:var(--text-muted); font-size:0.85rem;">Gestione las cuentas de acceso y permisos de administración del sistema.</p>
        </div>
        <button class="btn btn-primary" onclick="openUserModal()"><i class="fas fa-plus"></i> Registrar Usuario</button>
    </div>
</div>

<div class="card">
    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nombre de Usuario</th>
                    <th>Rol / Permiso</th>
                    <th>Estado</th>
                    <th style="width:130px; text-align:right;">Acciones</th>
                </tr>
            </thead>
            <tbody id="users-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: REGISTRAR/EDITAR USUARIO -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="user-modal" style="display:none;">
    <div class="modal-content" style="max-width:400px;">
        <div class="modal-header">
            <span class="modal-title" id="user-modal-title">Registrar Usuario</span>
            <button class="modal-close" onclick="closeUserModal()">&times;</button>
        </div>
        <form id="user-form">
            <input type="hidden" id="user-id">
            <div class="modal-body">
                <div class="form-group">
                    <label>Nombre de Usuario</label>
                    <input type="text" id="user-name" class="form-input" required placeholder="Ej: admin_olvan" autocomplete="off">
                </div>
                <div class="form-group">
                    <label>Rol de Usuario</label>
                    <select id="user-role-select" class="form-select" required>
                        <!-- Loaded dynamically -->
                    </select>
                </div>
                <div class="form-group">
                    <label id="user-password-label">Contraseña</label>
                    <input type="password" id="user-password" class="form-input" placeholder="••••••••">
                    <small style="color:var(--text-muted); font-size:0.75rem; display:block; margin-top:4px;" id="user-password-help">
                        Deje en blanco si no desea cambiar la contraseña actual.
                    </small>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" onclick="closeUserModal()">Cancelar</button>
                <button type="submit" class="btn btn-primary btn-sm">Guardar Usuario</button>
            </div>
        </form>
    </div>
</div>

<script>
// Users management logic
(function() {
    let users = [];
    let roles = [];

    window.initUsers = function() {
        loadRoles();
        loadUsers();

        document.getElementById('user-form').addEventListener('submit', e => {
            e.preventDefault();
            saveUser();
        });
    };

    function loadRoles() {
        fetch('controllers/users.php?action=list_roles')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    roles = res.data;
                    const sel = document.getElementById('user-role-select');
                    sel.innerHTML = '';
                    roles.forEach(role => {
                        sel.insertAdjacentHTML('beforeend', `<option value="${role.id_rol}">${role.nombre_rol}</option>`);
                    });
                }
            });
    }

    function loadUsers() {
        fetch('controllers/users.php?action=list')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    users = res.data;
                    renderUsersTable();
                }
            });
    }

    function renderUsersTable() {
        const tbody = document.getElementById('users-tbody');
        tbody.innerHTML = '';

        if (users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center" style="padding:20px; color:var(--text-light);">No hay usuarios registrados.</td></tr>`;
            return;
        }

        users.forEach(u => {
            const isActive = u.estado_usuario === 1 || u.estado_usuario === true || u.estado_usuario === '1';
            
            // Acciones condicionadas: No permitir desactivar propio usuario
            const isSelf = u.id_usuario == <?php echo $_SESSION['user_id']; ?>;
            let statusBtn = '';
            
            if (!isSelf) {
                if (isActive) {
                    statusBtn = `<button class="btn btn-danger btn-sm" onclick="deactivateUser(${u.id_usuario})" title="Desactivar"><i class="fas fa-user-slash"></i></button>`;
                } else {
                    statusBtn = `<button class="btn btn-primary btn-sm" onclick="activateUser(${u.id_usuario})" title="Activar"><i class="fas fa-user-check"></i></button>`;
                }
            }

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td>${u.id_usuario}</td>
                    <td class="bold">${u.nombre_usuario}</td>
                    <td>${u.nombre_rol}</td>
                    <td>
                        <span class="badge ${isActive ? 'badge-success' : 'badge-danger'}">
                            ${isActive ? 'Activo' : 'Inactivo'}
                        </span>
                    </td>
                    <td style="text-align:right;">
                        <button class="btn btn-blue btn-sm" onclick="editUser(${u.id_usuario})" title="Editar"><i class="fas fa-edit"></i></button>
                        ${statusBtn}
                    </td>
                </tr>
            `);
        });
    }

    // FORM DIALOGS
    window.openUserModal = function() {
        document.getElementById('user-modal').style.display = 'flex';
        document.getElementById('user-modal-title').textContent = 'Registrar Usuario';
        document.getElementById('user-form').reset();
        document.getElementById('user-id').value = '';
        document.getElementById('user-password-label').textContent = 'Contraseña';
        document.getElementById('user-password').required = true;
        document.getElementById('user-password-help').style.display = 'none';
    };

    window.closeUserModal = function() {
        document.getElementById('user-modal').style.display = 'none';
    };

    window.editUser = function(id) {
        const u = users.find(usr => usr.id_usuario == id);
        if (!u) return;

        openUserModal();
        document.getElementById('user-modal-title').textContent = 'Editar Usuario';
        document.getElementById('user-id').value = u.id_usuario;
        document.getElementById('user-name').value = u.nombre_usuario;
        document.getElementById('user-role-select').value = u.id_rol;
        document.getElementById('user-password-label').textContent = 'Nueva Contraseña (Opcional)';
        document.getElementById('user-password').required = false;
        document.getElementById('user-password-help').style.display = 'block';
    };

    function saveUser() {
        const id = document.getElementById('user-id').value;
        const name = document.getElementById('user-name').value;
        const role = document.getElementById('user-role-select').value;
        const pwd = document.getElementById('user-password').value;

        const fd = new FormData();
        fd.append('id_usuario', id);
        fd.append('nombre_usuario', name);
        fd.append('id_rol', role);
        fd.append('password_usuario', pwd);

        fetch('controllers/users.php?action=save', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadUsers();
                closeUserModal();
            } else {
                alert(res.message);
            }
        });
    }

    window.deactivateUser = function(id) {
        App.confirm('¿Seguro que desea desactivar esta cuenta de usuario?', () => {
            const fd = new FormData();
            fd.append('id_usuario', id);

            fetch('controllers/users.php?action=delete', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    loadUsers();
                } else {
                    alert(res.message);
                }
            });
        });
    };

    window.activateUser = function(id) {
        const fd = new FormData();
        fd.append('id_usuario', id);

        fetch('controllers/users.php?action=activate', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadUsers();
            } else {
                alert(res.message);
            }
        });
    };
})();
</script>
