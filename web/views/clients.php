<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="margin-bottom: 20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:15px;">
        <div>
            <h2><i class="fas fa-users" style="color:var(--primary);"></i> Gestión de Clientes</h2>
            <p style="color:var(--text-muted); font-size:0.85rem;">Registre y actualice la información de los clientes del sistema.</p>
        </div>
        <button class="btn btn-primary" onclick="openClientModal()"><i class="fas fa-user-plus"></i> Registrar Cliente</button>
    </div>
</div>

<div class="card">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px;">
        <input type="text" id="clie-search" class="pos-input" placeholder="Buscar por nombre o identidad..." oninput="renderClientsTable()">
        <label style="display:flex; align-items:center; gap:8px; font-size:0.9rem; font-weight:600; cursor:pointer;">
            <input type="checkbox" id="clie-active-only" onchange="loadClients()">
            Mostrar solo activos
        </label>
    </div>

    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Identidad / RTN</th>
                    <th>Nombre Completo</th>
                    <th>Teléfono</th>
                    <th>Correo Electrónico</th>
                    <th>Estado</th>
                    <th style="width:120px; text-align:right;">Acciones</th>
                </tr>
            </thead>
            <tbody id="clients-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: REGISTRAR/EDITAR CLIENTE -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="clie-modal" style="display:none;">
    <div class="modal-content">
        <div class="modal-header">
            <span class="modal-title" id="clie-modal-title">Registrar Cliente</span>
            <button class="modal-close" onclick="closeClientModal()">&times;</button>
        </div>
        <form id="clie-form">
            <input type="hidden" id="clie-id">
            <div class="modal-body">
                <div class="form-group">
                    <label>Identidad / RTN</label>
                    <input type="text" id="clie-identidad" class="form-input" required placeholder="0801-1990-12345">
                </div>
                <div class="form-group row">
                    <div>
                        <label>Nombre</label>
                        <input type="text" id="clie-nombre" class="form-input" required placeholder="Juan">
                    </div>
                    <div>
                        <label>Apellido</label>
                        <input type="text" id="clie-apellido" class="form-input" required placeholder="Pérez">
                    </div>
                </div>
                <div class="form-group">
                    <label>Teléfono</label>
                    <input type="text" id="clie-telefono" class="form-input" placeholder="9999-9999">
                </div>
                <div class="form-group">
                    <label>Correo Electrónico</label>
                    <input type="email" id="clie-correo" class="form-input" placeholder="juan.perez@ejemplo.com">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" onclick="closeClientModal()">Cancelar</button>
                <button type="submit" class="btn btn-primary btn-sm">Guardar Cliente</button>
            </div>
        </form>
    </div>
</div>

<script>
// Clients CRUD view logic
(function() {
    let clients = [];

    window.initClients = function() {
        loadClients();

        document.getElementById('clie-form').addEventListener('submit', e => {
            e.preventDefault();
            saveClient();
        });
    };

    window.loadClients = function() {
        const activeOnly = document.getElementById('clie-active-only').checked;
        fetch(`controllers/clients.php?action=list&active=${activeOnly}`)
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    clients = res.data;
                    renderClientsTable();
                }
            });
    };

    window.renderClientsTable = function() {
        const filter = document.getElementById('clie-search').value.toLowerCase();
        const tbody = document.getElementById('clients-tbody');
        tbody.innerHTML = '';

        const filtered = clients.filter(c => {
            const fullName = (c.nombre_cliente + ' ' + (c.apellido_cliente || '')).toLowerCase();
            return fullName.includes(filter) || c.identidad_cliente.includes(filter);
        });

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center" style="padding:20px; color:var(--text-light);">No se encontraron clientes.</td></tr>`;
            return;
        }

        filtered.forEach(c => {
            const fullName = c.nombre_cliente + ' ' + (c.apellido_cliente || '');
            const isActive = c.estado_cliente === 1 || c.estado_cliente === true || c.estado_cliente === '1';
            
            // Acciones condicionadas: No permitir desactivar consumidor final (ID 1)
            // Acciones condicionadas: No permitir modificar ni desactivar consumidor final (ID 1)
            let editBtn = '';
            let deleteBtn = '';
            if (c.id_cliente != 1) {
                editBtn = `<button class="btn btn-blue btn-sm" onclick="editClient(${c.id_cliente})" title="Editar"><i class="fas fa-user-edit"></i></button>`;
                if (isActive) {
                    deleteBtn = `<button class="btn btn-danger btn-sm" onclick="deactivateClient(${c.id_cliente})" title="Desactivar"><i class="fas fa-user-slash"></i></button>`;
                } else {
                    deleteBtn = `<button class="btn btn-primary btn-sm" onclick="activateClient(${c.id_cliente})" title="Activar"><i class="fas fa-user-check"></i></button>`;
                }
            }

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td class="bold">${c.identidad_cliente}</td>
                    <td>${fullName}</td>
                    <td>${c.telefono_cliente || 'N/A'}</td>
                    <td>${c.correo_cliente || 'N/A'}</td>
                    <td>
                        <span class="badge ${isActive ? 'badge-success' : 'badge-danger'}">
                            ${isActive ? 'Activo' : 'Inactivo'}
                        </span>
                    </td>
                    <td style="text-align:right; gap:4px;">
                        ${editBtn}
                        ${deleteBtn}
                    </td>
                </tr>
            `);
        });
    };

    // FORM DIALOG ACTIONS
    window.openClientModal = function() {
        document.getElementById('clie-modal').style.display = 'flex';
        document.getElementById('clie-modal-title').textContent = 'Registrar Cliente';
        document.getElementById('clie-form').reset();
        document.getElementById('clie-id').value = '';
        document.getElementById('clie-identidad').disabled = false;
    };

    window.closeClientModal = function() {
        document.getElementById('clie-modal').style.display = 'none';
    };

    window.editClient = function(id) {
        const c = clients.find(cl => cl.id_cliente == id);
        if (!c) return;

        openClientModal();
        document.getElementById('clie-modal-title').textContent = 'Editar Cliente';
        document.getElementById('clie-id').value = c.id_cliente;
        document.getElementById('clie-identidad').value = c.identidad_cliente;
        document.getElementById('clie-nombre').value = c.nombre_cliente;
        document.getElementById('clie-apellido').value = c.apellido_cliente || '';
        document.getElementById('clie-telefono').value = c.telefono_cliente || '';
        document.getElementById('clie-correo').value = c.correo_cliente || '';
        
        // No editar identidad de Consumidor Final
        if (c.id_cliente == 1) {
            document.getElementById('clie-identidad').disabled = true;
        }
    };

    function saveClient() {
        const id = document.getElementById('clie-id').value;
        const identidad = document.getElementById('clie-identidad').value;
        const nombre = document.getElementById('clie-nombre').value;
        const apellido = document.getElementById('clie-apellido').value;
        const telefono = document.getElementById('clie-telefono').value;
        const correo = document.getElementById('clie-correo').value;

        const fd = new FormData();
        fd.append('id_cliente', id);
        fd.append('identidad_cliente', identidad);
        fd.append('nombre_cliente', nombre);
        fd.append('apellido_cliente', apellido);
        fd.append('telefono_cliente', telefono);
        fd.append('correo_cliente', correo);

        fetch('controllers/clients.php?action=save', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadClients();
                closeClientModal();
            } else {
                alert(res.message);
            }
        });
    }

    window.deactivateClient = function(id) {
        App.confirm('¿Seguro que desea desactivar este cliente?', () => {
            const fd = new FormData();
            fd.append('id_cliente', id);

            fetch('controllers/clients.php?action=delete', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    loadClients();
                } else {
                    alert(res.message);
                }
            });
        });
    };

    window.activateClient = function(id) {
        const fd = new FormData();
        fd.append('id_cliente', id);

        fetch('controllers/clients.php?action=activate', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadClients();
            } else {
                alert(res.message);
            }
        });
    };
})();
</script>
