<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="padding:15px; margin-bottom: 20px;">
    <!-- Sub-navigation Tabs -->
    <div style="display:flex; gap:10px; border-bottom: 1px solid var(--border-color); padding-bottom:10px;">
        <button class="btn btn-secondary tab-btn active" onclick="switchInventoryTab('products')"><i class="fas fa-boxes"></i> Productos</button>
        <button class="btn btn-secondary tab-btn" onclick="switchInventoryTab('categories')"><i class="fas fa-tags"></i> Categorías</button>
        <button class="btn btn-secondary tab-btn" onclick="switchInventoryTab('providers')"><i class="fas fa-truck"></i> Proveedores</button>
        <button class="btn btn-secondary tab-btn" onclick="switchInventoryTab('locations')"><i class="fas fa-map-marker-alt"></i> Ubicaciones</button>
    </div>
</div>

<!-- ========================================================= -->
<!-- TAB: PRODUCTOS -->
<!-- ========================================================= -->
<div id="tab-products" class="tab-content-panel">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
        <input type="text" id="inv-prod-search" class="pos-input" placeholder="Buscar producto..." oninput="renderProductsTable()">
        <?php if ($_SESSION['role_id'] != 3): ?>
        <button class="btn btn-primary" onclick="openProductForm()"><i class="fas fa-plus"></i> Nuevo Producto</button>
        <?php endif; ?>
    </div>
    
    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width:70px;">Foto</th>
                    <th>Código</th>
                    <th>Descripción</th>
                    <th>Precios (Compra/Venta)</th>
                    <th>Stock / Mínimo</th>
                    <th>Ubicación</th>
                    <th style="width:180px; text-align:right;">Acciones</th>
                </tr>
            </thead>
            <tbody id="inv-products-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- TAB: CATEGORÍAS -->
<!-- ========================================================= -->
<div id="tab-categories" class="tab-content-panel" style="display:none;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
        <h3 style="font-size:1.1rem;"><i class="fas fa-tags" style="color:var(--primary)"></i> Categorías de Productos</h3>
        <button class="btn btn-primary" onclick="openCategoryForm()"><i class="fas fa-plus"></i> Nueva Categoría</button>
    </div>
    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Categoría</th>
                    <th>Descripción</th>
                    <th>Días Garantía</th>
                    <th style="text-align:right;">Acción</th>
                </tr>
            </thead>
            <tbody id="inv-categories-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- TAB: PROVEEDORES -->
<!-- ========================================================= -->
<div id="tab-providers" class="tab-content-panel" style="display:none;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
        <h3 style="font-size:1.1rem;"><i class="fas fa-truck" style="color:var(--primary)"></i> Proveedores</h3>
        <button class="btn btn-primary" onclick="openProviderForm()"><i class="fas fa-plus"></i> Nuevo Proveedor</button>
    </div>
    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Proveedor</th>
                    <th>Encargado</th>
                    <th>Teléfono</th>
                    <th>Dirección</th>
                    <th>Repuestos</th>
                    <th style="text-align:right;">Acción</th>
                </tr>
            </thead>
            <tbody id="inv-providers-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- TAB: UBICACIONES -->
<!-- ========================================================= -->
<div id="tab-locations" class="tab-content-panel" style="display:none;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
        <h3 style="font-size:1.1rem;"><i class="fas fa-map-marker-alt" style="color:var(--primary)"></i> Ubicaciones</h3>
        <button class="btn btn-primary" onclick="openLocationForm()"><i class="fas fa-plus"></i> Nueva Ubicación</button>
    </div>
    <div class="data-table-container" style="max-width: 500px;">
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Ubicación</th>
                    <th style="text-align:right;">Acción</th>
                </tr>
            </thead>
            <tbody id="inv-locations-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: CREAR/EDITAR PRODUCTO -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="inv-prod-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title" id="inv-prod-modal-title">Registrar Producto</span>
            <button class="modal-close" onclick="closeProductForm()">&times;</button>
        </div>
        <form id="inv-prod-form">
            <input type="hidden" id="prod-id">
            <div class="modal-body">
                <div class="form-group row">
                    <div>
                        <label>Código de Barras</label>
                        <input type="text" id="prod-barcode" class="form-input" placeholder="Escanear o autogenerado...">
                    </div>
                    <div>
                        <label>Nombre del Producto</label>
                        <input type="text" id="prod-name" class="form-input" required placeholder="Repuesto / Celular...">
                    </div>
                </div>

                <div class="form-group row">
                    <div>
                        <label>Categoría</label>
                        <select id="prod-category" class="form-select" required></select>
                    </div>
                    <div>
                        <label>Proveedor</label>
                        <select id="prod-provider" class="form-select" required></select>
                    </div>
                    <div>
                        <label>Ubicación</label>
                        <select id="prod-location" class="form-select" required></select>
                    </div>
                </div>

                <div class="form-group row">
                    <div>
                        <label>Precio Compra</label>
                        <input type="number" step="0.01" id="prod-price-buy" class="form-input" required placeholder="L 0.00">
                    </div>
                    <div>
                        <label>Precio Venta</label>
                        <input type="number" step="0.01" id="prod-price-sell" class="form-input" required placeholder="L 0.00">
                    </div>
                    <div>
                        <label>Precio Mayorista (Opcional)</label>
                        <input type="number" step="0.01" id="prod-price-wholesale" class="form-input" placeholder="L 0.00">
                    </div>
                </div>

                <div class="form-group row">
                    <div>
                        <label>Stock Mínimo</label>
                        <input type="number" id="prod-stock-min" class="form-input" required placeholder="5">
                    </div>
                    <div id="prod-stock-container">
                        <label>Stock Inicial</label>
                        <input type="number" id="prod-stock" class="form-input" required placeholder="10">
                    </div>
                    <div>
                        <label>Garantía (Días)</label>
                        <input type="number" id="prod-warranty" class="form-input" placeholder="0">
                    </div>
                </div>

                <div class="form-group row">
                    <div style="flex:1;">
                        <label>Imagen del Producto</label>
                        <input type="file" id="prod-image-file" class="form-input" onchange="uploadProductImage()">
                        <input type="hidden" id="prod-image-url">
                    </div>
                    <div style="flex:0 0 100px; display:flex; align-items:center; justify-content:center; border:1px solid var(--border-color); border-radius:var(--border-radius-md); overflow:hidden;">
                        <img id="prod-image-preview" src="https://cdn-icons-png.flaticon.com/512/869/869045.png" style="width:100%; height:100%; object-fit:cover;">
                    </div>
                </div>

                <div class="form-group">
                    <label style="display:flex; align-items:center; gap:8px; cursor:pointer;">
                        <input type="checkbox" id="prod-require-serial" value="1">
                        ¿Requiere Serie / IMEI?
                    </label>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" onclick="closeProductForm()">Cancelar</button>
                <button type="submit" class="btn btn-primary btn-sm">Guardar Producto</button>
            </div>
        </form>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: KARDEX & AJUSTES -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="inv-kardex-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title" id="kardex-modal-title">Kardex del Producto</span>
            <button class="modal-close" onclick="closeKardexModal()">&times;</button>
        </div>
        <div class="modal-body">
            <!-- Botón para ajuste manual -->
            <?php if ($_SESSION['role_id'] != 3): ?>
            <div style="display:flex; justify-content:flex-end; margin-bottom:15px;">
                <button class="btn btn-blue btn-sm" onclick="openKardexAdjustment()"><i class="fas fa-exchange-alt"></i> Registrar Ajuste Manual</button>
            </div>
            <?php endif; ?>

            <!-- Tabla de historial -->
            <div style="max-height: 300px; overflow-y: auto; border:1px solid var(--border-color); border-radius: var(--border-radius-md);">
                <table class="data-table" style="font-size:0.825rem;">
                    <thead>
                        <tr>
                            <th>Fecha</th>
                            <th>Movimiento</th>
                            <th>Cant.</th>
                            <th>Stock Restante</th>
                            <th>Observación / Referencia</th>
                            <th>Usuario</th>
                        </tr>
                    </thead>
                    <tbody id="kardex-history-tbody">
                        <!-- Loaded dynamically -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: FORMULARIO DE AJUSTE KARDEX -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="inv-adj-modal" style="display:none;">
    <div class="modal-content" style="max-width:400px;">
        <div class="modal-header">
            <span class="modal-title">Ajuste de Inventario</span>
            <button class="modal-close" onclick="closeKardexAdjustment()">&times;</button>
        </div>
        <form id="inv-adj-form">
            <div class="modal-body">
                <div style="font-size:0.85rem; margin-bottom:12px; color:var(--text-muted);" id="adj-product-name-lbl">
                    Producto: <strong style="color:var(--text-main)"></strong>
                </div>
                <div class="form-group">
                    <label>Tipo de Ajuste</label>
                    <select id="adj-type" class="form-select" required>
                        <option value="Entrada">Entrada (Sumar al stock)</option>
                        <option value="Salida">Salida (Restar del stock)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Cantidad</label>
                    <input type="number" id="adj-qty" class="form-input" required min="1" placeholder="10">
                </div>
                <div class="form-group">
                    <label>Observación / Justificación</label>
                    <textarea id="adj-obs" class="form-textarea" required placeholder="Ej: Ajuste por inventario físico..." style="height:80px;"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" onclick="closeKardexAdjustment()">Cancelar</button>
                <button type="submit" class="btn btn-primary btn-sm">Confirmar Ajuste</button>
            </div>
        </form>
    </div>
</div>

<!-- Support metadata forms (Category, Provider, Location modals) -->
<!-- Simplified dynamic creation or simple blocks can go here -->

<script>
// Inventory management code
(function() {
    let products = [];
    let categories = [];
    let providers = [];
    let locations = [];
    let activeKardexProduct = null;

    window.initInventory = function() {
        switchInventoryTab('products');
        loadMetadata();
        loadProducts();

        // Product Form submit
        document.getElementById('inv-prod-form').addEventListener('submit', e => {
            e.preventDefault();
            saveProduct();
        });

        // Adjustment Form submit
        document.getElementById('inv-adj-form').addEventListener('submit', e => {
            e.preventDefault();
            saveKardexAdjustment();
        });
    };

    window.switchInventoryTab = function(tabName) {
        // Toggle tab active class
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        const btn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.textContent.toLowerCase().includes(tabName === 'products' ? 'productos' : tabName === 'categories' ? 'categorías' : tabName === 'providers' ? 'proveedores' : 'ubicaciones'));
        if (btn) btn.classList.add('active');

        // Toggle panel display
        document.querySelectorAll('.tab-content-panel').forEach(p => p.style.display = 'none');
        document.getElementById(`tab-${tabName}`).style.display = 'block';

        if (tabName === 'categories') loadCategories();
        else if (tabName === 'providers') loadProviders();
        else if (tabName === 'locations') loadLocations();
        else loadProducts();
    };

    function loadMetadata() {
        // Categories
        fetch('controllers/inventory.php?action=list_categories')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    categories = res.data;
                    const sel = document.getElementById('prod-category');
                    sel.innerHTML = '';
                    categories.forEach(c => sel.insertAdjacentHTML('beforeend', `<option value="${c.id_categoria}">${c.nombre_categoria}</option>`));
                }
            });

        // Providers
        fetch('controllers/inventory.php?action=list_providers')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    providers = res.data;
                    const sel = document.getElementById('prod-provider');
                    sel.innerHTML = '';
                    providers.forEach(p => sel.insertAdjacentHTML('beforeend', `<option value="${p.id_proveedor}">${p.nombre_proveedor}</option>`));
                }
            });

        // Locations
        fetch('controllers/inventory.php?action=list_locations')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    locations = res.data;
                    const sel = document.getElementById('prod-location');
                    sel.innerHTML = '';
                    locations.forEach(l => sel.insertAdjacentHTML('beforeend', `<option value="${l.id_ubicacion}">${l.nombre_ubicacion}</option>`));
                }
            });
    }

    function loadProducts() {
        fetch('controllers/inventory.php?action=list')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    products = res.data;
                    renderProductsTable();
                }
            });
    }

    window.renderProductsTable = function() {
        const filter = document.getElementById('inv-prod-search').value.toLowerCase();
        const tbody = document.getElementById('inv-products-tbody');
        tbody.innerHTML = '';

        const filtered = products.filter(p => {
            return p.nombre_producto.toLowerCase().includes(filter) || 
                   (p.codigo_barras_producto && p.codigo_barras_producto.toLowerCase().includes(filter));
        });

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding:20px; color:var(--text-light);">No se encontraron productos.</td></tr>`;
            return;
        }

        filtered.forEach(p => {
            const img = p.ruta_imagen_producto ? p.ruta_imagen_producto : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
            const wholesaleLabel = p.precio_mayorista_producto > 0 ? `<br><small style="color:var(--text-muted);">Mayor: L ${parseFloat(p.precio_mayorista_producto).toFixed(2)}</small>` : '';
            
            let actionButtons = '';
            if (typeof CURRENT_ROLE_ID !== 'undefined' && CURRENT_ROLE_ID == 3) {
                actionButtons = `
                    <button class="btn btn-secondary btn-sm" onclick="openKardex(${p.id_producto}, '${p.nombre_producto}')" title="Ver Kardex"><i class="fas fa-history"></i></button>
                `;
            } else {
                actionButtons = `
                    <button class="btn btn-success btn-sm" onclick="openDirectAdjustment(${p.id_producto}, '${p.nombre_producto}')" title="Ingresar/Ajustar Stock" style="background:#27ae60; border-color:#27ae60;"><i class="fas fa-plus-circle"></i></button>
                    <button class="btn btn-secondary btn-sm" onclick="openKardex(${p.id_producto}, '${p.nombre_producto}')" title="Ver Kardex"><i class="fas fa-history"></i></button>
                    <button class="btn btn-blue btn-sm" onclick="editProduct(${p.id_producto})"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-danger btn-sm" onclick="deleteProduct(${p.id_producto})"><i class="fas fa-trash-alt"></i></button>
                `;
            }

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td><img src="${img}" style="width:40px; height:40px; object-fit:cover; border-radius:6px; border:1px solid var(--border-color); cursor:pointer;" onclick="App.showImagePreview('${img}')" onerror="this.src='https://cdn-icons-png.flaticon.com/512/869/869045.png'"></td>
                    <td class="bold">${p.codigo_barras_producto || 'N/A'}</td>
                    <td>
                        <div style="font-weight:600;">${p.nombre_producto}</div>
                        <small style="color:var(--text-muted);">${p.nombre_categoria || 'Sin Categoría'}</small>
                    </td>
                    <td>
                        Compra: L ${parseFloat(p.precio_compra_producto).toFixed(2)}<br>
                        Venta: L ${parseFloat(p.precio_venta_producto).toFixed(2)}
                        ${wholesaleLabel}
                    </td>
                    <td>
                        <span class="badge ${p.stock_producto > p.stock_minimo_producto ? 'badge-success' : 'badge-danger'}">
                            ${p.stock_producto}
                        </span>
                        / <small style="color:var(--text-light);">${p.stock_minimo_producto}</small>
                    </td>
                    <td>${p.nombre_ubicacion || 'N/A'}</td>
                    <td style="text-align:right; gap:4px; display:flex; justify-content:flex-end;">
                        ${actionButtons}
                    </td>
                </tr>
            `);
        });
    };

    // SAVE PRODUCT
    window.openProductForm = function() {
        document.getElementById('inv-prod-modal').style.display = 'flex';
        document.getElementById('inv-prod-modal-title').textContent = 'Registrar Producto';
        document.getElementById('inv-prod-form').reset();
        document.getElementById('prod-id').value = '';
        document.getElementById('prod-stock-container').style.display = 'block';
        document.getElementById('prod-image-preview').src = 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
        document.getElementById('prod-image-url').value = '';
    };

    window.closeProductForm = function() {
        document.getElementById('inv-prod-modal').style.display = 'none';
    };

    window.uploadProductImage = function() {
        const fileInput = document.getElementById('prod-image-file');
        if (fileInput.files.length === 0) return;

        const fd = new FormData();
        fd.append('image', fileInput.files[0]);

        fetch('controllers/inventory.php?action=upload_image', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                document.getElementById('prod-image-url').value = res.url;
                document.getElementById('prod-image-preview').src = res.url;
                App.showToast('Imagen subida con éxito.');
            } else {
                alert(res.message);
            }
        });
    };

    function saveProduct() {
        const id = document.getElementById('prod-id').value;
        const bc = document.getElementById('prod-barcode').value;
        const name = document.getElementById('prod-name').value;
        const cat = document.getElementById('prod-category').value;
        const prov = document.getElementById('prod-provider').value;
        const loc = document.getElementById('prod-location').value;
        const buy = document.getElementById('prod-price-buy').value;
        const sell = document.getElementById('prod-price-sell').value;
        const whole = document.getElementById('prod-price-wholesale').value;
        const min = document.getElementById('prod-stock-min').value;
        const stock = document.getElementById('prod-stock').value;
        const warranty = document.getElementById('prod-warranty').value;
        const image = document.getElementById('prod-image-url').value;
        const requireSerial = document.getElementById('prod-require-serial').checked ? 1 : 0;

        const fd = new FormData();
        fd.append('id_producto', id);
        fd.append('codigo_barras_producto', bc);
        fd.append('nombre_producto', name);
        fd.append('id_categoria', cat);
        fd.append('id_proveedor', prov);
        fd.append('id_ubicacion', loc);
        fd.append('precio_compra_producto', buy);
        fd.append('precio_venta_producto', sell);
        fd.append('precio_mayorista_producto', whole);
        fd.append('stock_minimo_producto', min);
        fd.append('stock_producto', stock);
        fd.append('dias_garantia', warranty);
        fd.append('ruta_imagen_producto', image);
        fd.append('requiere_serie', requireSerial);

        fetch('controllers/inventory.php?action=save', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadProducts();
                closeProductForm();
            } else {
                alert(res.message);
            }
        });
    }

    window.editProduct = function(id) {
        const p = products.find(prod => prod.id_producto == id);
        if (!p) return;

        openProductForm();
        document.getElementById('inv-prod-modal-title').textContent = 'Editar Producto';
        document.getElementById('prod-id').value = p.id_producto;
        document.getElementById('prod-barcode').value = p.codigo_barras_producto || '';
        document.getElementById('prod-name').value = p.nombre_producto;
        document.getElementById('prod-category').value = p.id_categoria;
        document.getElementById('prod-provider').value = p.id_proveedor;
        document.getElementById('prod-location').value = p.id_ubicacion;
        document.getElementById('prod-price-buy').value = p.precio_compra_producto;
        document.getElementById('prod-price-sell').value = p.precio_venta_producto;
        document.getElementById('prod-price-wholesale').value = p.precio_mayorista_producto || '';
        document.getElementById('prod-stock-min').value = p.stock_minimo_producto;
        document.getElementById('prod-stock').value = p.stock_producto;
        document.getElementById('prod-stock-container').style.display = 'none'; // Ocultar stock inicial al editar
        document.getElementById('prod-warranty').value = p.dias_garantia || 0;
        document.getElementById('prod-require-serial').checked = (p.requiere_serie === 1 || p.requiere_serie === true || p.requiere_serie === '1');
        document.getElementById('prod-image-url').value = p.ruta_imagen_producto || '';
        document.getElementById('prod-image-preview').src = p.ruta_imagen_producto ? p.ruta_imagen_producto : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
    };

    window.deleteProduct = function(id) {
        App.confirm('¿Seguro que desea eliminar lógicamente este producto del inventario?', () => {
            const fd = new FormData();
            fd.append('id_producto', id);

            fetch('controllers/inventory.php?action=delete', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    loadProducts();
                } else {
                    alert(res.message);
                }
            });
        });
    };

    // KARDEX HISTORY & ADJUSTMENT LOGIC
    window.openKardex = function(id, name) {
        activeKardexProduct = id;
        document.getElementById('kardex-modal-title').textContent = `Kardex: ${name}`;
        document.getElementById('inv-kardex-modal').style.display = 'flex';
        loadKardexHistory();
    };

    window.closeKardexModal = function() {
        document.getElementById('inv-kardex-modal').style.display = 'none';
        activeKardexProduct = null;
    };

    function loadKardexHistory() {
        if (!activeKardexProduct) return;
        fetch(`controllers/inventory.php?action=get_kardex&id_producto=${activeKardexProduct}`)
            .then(r => r.json())
            .then(res => {
                const tbody = document.getElementById('kardex-history-tbody');
                tbody.innerHTML = '';
                if (res.success) {
                    if (res.data.length === 0) {
                        tbody.innerHTML = `<tr><td colspan="6" class="text-center" style="padding:15px; color:var(--text-light);">Sin movimientos registrados.</td></tr>`;
                        return;
                    }
                    res.data.forEach(k => {
                        const date = new Date(k.fecha_movimiento_producto).toLocaleDateString('es-HN') + ' ' + new Date(k.fecha_movimiento_producto).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'});
                        tbody.insertAdjacentHTML('beforeend', `
                            <tr>
                                <td>${date}</td>
                                <td><span class="badge ${k.tipo_movimiento_producto === 'Entrada' ? 'badge-success' : 'badge-danger'}">${k.tipo_movimiento_producto}</span></td>
                                <td class="bold">${k.cantidad_producto}</td>
                                <td>${k.stock_restante_producto}</td>
                                <td>${k.referencia_producto}</td>
                                <td>${k.nombre_usuario}</td>
                            </tr>
                        `);
                    });
                }
            });
    }

    window.openDirectAdjustment = function(id, name) {
        activeKardexProduct = id;
        document.getElementById('inv-adj-modal').style.display = 'flex';
        document.getElementById('inv-adj-form').reset();
        document.getElementById('adj-product-name-lbl').querySelector('strong').textContent = name;
    };

    window.openKardexAdjustment = function() {
        document.getElementById('inv-adj-modal').style.display = 'flex';
        document.getElementById('inv-adj-form').reset();
        // Obtener el nombre del producto de la cabecera
        const titleText = document.getElementById('kardex-modal-title').textContent;
        const name = titleText.replace('Kardex del Producto: ', '').replace('Kardex: ', '');
        document.getElementById('adj-product-name-lbl').querySelector('strong').textContent = name;
    };

    window.closeKardexAdjustment = function() {
        document.getElementById('inv-adj-modal').style.display = 'none';
    };

    function saveKardexAdjustment() {
        // Pedir firma electrónica (Contraseña)
        App.askSignature((userId, username) => {
            const type = document.getElementById('adj-type').value;
            const qty = document.getElementById('adj-qty').value;
            const obs = document.getElementById('adj-obs').value;

            const fd = new FormData();
            fd.append('id_producto', activeKardexProduct);
            fd.append('tipo_movimiento_producto', type);
            fd.append('cantidad_producto', qty);
            fd.append('referencia_producto', obs);
            fd.append('id_usuario_firma', userId);

            fetch('controllers/inventory.php?action=save_kardex', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    loadKardexHistory();
                    loadProducts(); // Recargar stock en listado
                    closeKardexAdjustment();
                } else {
                    alert(res.message);
                }
            });
        });
    }

    // CATEGORIES, PROVIDERS, LOCATIONS METADATA LOADING/RENDERING
    // simplified templates rendering...
    function loadCategories() {
        fetch('controllers/inventory.php?action=list_categories')
            .then(r => r.json())
            .then(res => {
                const tbody = document.getElementById('inv-categories-tbody');
                tbody.innerHTML = '';
                if (res.success) {
                    res.data.forEach(c => {
                        tbody.insertAdjacentHTML('beforeend', `
                            <tr>
                                <td>${c.id_categoria}</td>
                                <td class="bold">${c.nombre_categoria}</td>
                                <td>${c.descripcion_categoria || 'Sin descripción'}</td>
                                <td>${c.dias_garantias || 0} días</td>
                                <td style="text-align:right;">
                                    <button class="btn btn-secondary btn-sm" onclick="editCategory(${c.id_categoria}, '${c.nombre_categoria}', '${c.descripcion_categoria}', ${c.dias_garantias})"><i class="fas fa-edit"></i></button>
                                </td>
                            </tr>
                        `);
                    });
                }
            });
    }

    window.openCategoryForm = function() {
        const name = prompt('Nombre de la Categoría:');
        if (!name) return;
        const desc = prompt('Descripción:');
        const days = parseInt(prompt('Días de Garantía por Defecto:', '0')) || 0;
        saveCategory(0, name, desc, days);
    };

    window.editCategory = function(id, name, desc, days) {
        const newName = prompt('Nombre de la Categoría:', name);
        if (!newName) return;
        const newDesc = prompt('Descripción:', desc);
        const newDays = parseInt(prompt('Días de Garantía por Defecto:', days)) || 0;
        saveCategory(id, newName, newDesc, newDays);
    };

    function saveCategory(id, name, desc, days) {
        const fd = new FormData();
        fd.append('id_categoria', id);
        fd.append('nombre_categoria', name);
        fd.append('descripcion_categoria', desc);
        fd.append('dias_garantias', days);

        fetch('controllers/inventory.php?action=save_category', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadCategories();
                loadMetadata();
            }
        });
    }

    function loadProviders() {
        fetch('controllers/inventory.php?action=list_providers')
            .then(r => r.json())
            .then(res => {
                const tbody = document.getElementById('inv-providers-tbody');
                tbody.innerHTML = '';
                if (res.success) {
                    res.data.forEach(p => {
                        tbody.insertAdjacentHTML('beforeend', `
                            <tr>
                                <td class="bold">${p.nombre_provider || p.nombre_proveedor}</td>
                                <td>${p.nombre_encargado_proveedor || ''}</td>
                                <td>${p.telefono_proveedor || ''}</td>
                                <td>${p.direccion_proveedor || ''}</td>
                                <td>${p.tipo_repuestos_proveedor || ''}</td>
                                <td style="text-align:right;">
                                    <button class="btn btn-secondary btn-sm" onclick="editProvider(${p.id_proveedor})"><i class="fas fa-edit"></i></button>
                                </td>
                            </tr>
                        `);
                    });
                }
            });
    }

    window.openProviderForm = function() {
        // Simple prompt based CRUD or custom form
        const name = prompt('Nombre del Proveedor:');
        if (!name) return;
        saveProvider(0, name);
    };

    window.editProvider = function(id) {
        const p = providers.find(prod => prod.id_proveedor == id);
        if (!p) return;
        const name = prompt('Editar Nombre:', p.nombre_proveedor);
        if (!name) return;
        saveProvider(id, name, p.nombre_encargado_proveedor, p.telefono_proveedor, p.direccion_proveedor, p.tipo_repuestos_proveedor);
    };

    function saveProvider(id, name, encargado='', tel='', dir='', repuestos='') {
        const fd = new FormData();
        fd.append('id_proveedor', id);
        fd.append('nombre_proveedor', name);
        fd.append('nombre_encargado_proveedor', encargado);
        fd.append('telefono_proveedor', tel);
        fd.append('direccion_proveedor', dir);
        fd.append('tipo_repuestos_proveedor', repuestos);

        fetch('controllers/inventory.php?action=save_provider', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadProviders();
                loadMetadata();
            }
        });
    }

    function loadLocations() {
        fetch('controllers/inventory.php?action=list_locations')
            .then(r => r.json())
            .then(res => {
                const tbody = document.getElementById('inv-locations-tbody');
                tbody.innerHTML = '';
                if (res.success) {
                    res.data.forEach(l => {
                        tbody.insertAdjacentHTML('beforeend', `
                            <tr>
                                <td>${l.id_ubicacion}</td>
                                <td class="bold">${l.nombre_ubicacion}</td>
                                <td style="text-align:right;">
                                    <button class="btn btn-secondary btn-sm" onclick="editLocation(${l.id_ubicacion}, '${l.nombre_ubicacion}')"><i class="fas fa-edit"></i></button>
                                </td>
                            </tr>
                        `);
                    });
                }
            });
    }

    window.openLocationForm = function() {
        const name = prompt('Nombre de la Ubicación:');
        if (!name) return;
        saveLocation(0, name);
    };

    window.editLocation = function(id, name) {
        const newName = prompt('Editar Ubicación:', name);
        if (!newName) return;
        saveLocation(id, newName);
    };

    function saveLocation(id, name) {
        const fd = new FormData();
        fd.append('id_ubicacion', id);
        fd.append('nombre_ubicacion', name);

        fetch('controllers/inventory.php?action=save_location', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadLocations();
                loadMetadata();
            }
        });
    }
})();
</script>
