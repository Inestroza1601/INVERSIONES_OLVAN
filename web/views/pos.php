<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="pos-layout">
    <!-- Left Cart Panel -->
    <div class="pos-cart-panel">
        <div class="pos-cart-header">
            <!-- Client Selection -->
            <div style="display:flex; align-items:center; gap:12px;">
                <span style="font-weight:600; color:var(--text-muted);">Cliente:</span>
                <span id="pos-selected-client-name" style="font-weight:700; color:var(--text-main);">CONSUMIDOR FINAL</span>
                <button class="btn btn-secondary btn-sm" onclick="posOpenClientSearch()"><i class="fas fa-search"></i> Buscar</button>
                <button class="btn btn-primary btn-sm" onclick="posOpenNewClientModal()"><i class="fas fa-plus"></i> Nuevo</button>
            </div>
            
            <!-- Scan / Catalog Search -->
            <div class="pos-search-bar">
                <input type="text" id="pos-scan-input" class="pos-input" placeholder="Escanear código de barras...">
                <button class="btn btn-blue btn-sm" onclick="posOpenCatalog()"><i class="fas fa-th-list"></i> Catálogo</button>
            </div>
        </div>

        <!-- Cart Table Container -->
        <div class="pos-cart-body">
            <table class="pos-table" id="pos-cart-table">
                <thead>
                    <tr>
                        <th style="width:70px;">Foto</th>
                        <th>Producto</th>
                        <th style="width:130px; text-align:center;">Cant.</th>
                        <th style="width:120px; text-align:right;">Precio</th>
                        <th style="width:120px; text-align:right;">Subtotal</th>
                        <th style="width:60px;"></th>
                    </tr>
                </thead>
                <tbody id="pos-cart-tbody">
                    <!-- Cart items loaded dynamically -->
                    <tr>
                        <td colspan="6" class="text-center" style="padding:40px; color:var(--text-light);">
                            <i class="fas fa-shopping-basket" style="font-size:3rem; display:block; margin-bottom:12px;"></i>
                            El carrito está vacío. Escanee un código o abra el Catálogo.
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Right Checkout Panel -->
    <div class="pos-checkout-panel">
        <div class="checkout-header">
            <h3><i class="fas fa-cash-register" style="color:var(--primary)"></i> Cobrar Venta</h3>
        </div>

        <div class="checkout-form-group">
            <label>Método de Pago</label>
            <select id="pos-pay-method" class="checkout-select">
                <!-- Methods loaded dynamically -->
            </select>
        </div>

        <!-- Extra bank/reference fields for Card or Transfer -->
        <div id="pos-extra-fields" class="extra-fields" style="display:none;">
            <div class="checkout-form-group">
                <label>Banco Emisor</label>
                <select id="pos-pay-bank" class="checkout-select">
                    <option>Seleccione Banco...</option>
                    <option>Banco Atlántida</option>
                    <option>BAC Credomatic</option>
                    <option>Banco Ficohsa</option>
                    <option>Banpaís</option>
                    <option>Banco de Occidente</option>
                    <option>Banco Banrural</option>
                    <option>Banco Promerica</option>
                    <option>Banco LAFISE</option>
                    <option>ACH - Transferencia Interbancaria</option>
                </select>
            </div>
            <div class="checkout-form-group" style="margin-bottom:0;">
                <label id="pos-ref-label">Nº Referencia:</label>
                <input type="text" id="pos-pay-ref" class="checkout-input" placeholder="Nº de transacción / voucher">
            </div>
        </div>

        <!-- Receipt Pie Summary -->
        <div class="checkout-summary">
            <div class="summary-row">
                <span>Subtotal:</span>
                <span id="pos-subtotal-label">L 0.00</span>
            </div>
            <div class="summary-row">
                <span>Impuesto (15%):</span>
                <span id="pos-tax-label">L 0.00</span>
            </div>
            <div class="summary-row total">
                <span>Total a Pagar:</span>
                <span id="pos-total-label" class="total-amount">L 0.00</span>
            </div>
            
            <button class="btn-cobrar" onclick="posProcessCheckout()">
                <i class="fas fa-check"></i> Completar Venta
            </button>
        </div>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: BUSCAR CLIENTE -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="pos-client-modal" style="display:none;">
    <div class="modal-content">
        <div class="modal-header">
            <span class="modal-title"><i class="fas fa-users" style="color:var(--primary)"></i> Buscar Cliente</span>
            <button class="modal-close" onclick="posCloseClientSearch()">&times;</button>
        </div>
        <div class="modal-body" style="padding:15px 24px;">
            <input type="text" id="pos-client-filter" class="form-input" placeholder="Buscar por nombre o identidad..." style="margin-bottom:15px;" oninput="posRenderClientList()">
            <div style="max-height:300px; overflow-y:auto; border:1px solid var(--border-color); border-radius:var(--border-radius-md);">
                <table class="data-table" style="font-size:0.85rem;">
                    <thead>
                        <tr>
                            <th>Identidad</th>
                            <th>Nombre</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody id="pos-client-table-body">
                        <!-- Loaded dynamically -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: REGISTRAR NUEVO CLIENTE -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="pos-new-client-modal" style="display:none;">
    <div class="modal-content">
        <div class="modal-header">
            <span class="modal-title"><i class="fas fa-user-plus" style="color:var(--primary)"></i> Registrar Nuevo Cliente</span>
            <button class="modal-close" onclick="posCloseNewClientModal()">&times;</button>
        </div>
        <form id="pos-new-client-form">
            <div class="modal-body">
                <div class="form-group">
                    <label>Identidad / RTN</label>
                    <input type="text" id="pos-nc-identidad" class="form-input" required placeholder="0801-1990-12345">
                </div>
                <div class="form-group row">
                    <div>
                        <label>Nombre</label>
                        <input type="text" id="pos-nc-nombre" class="form-input" required placeholder="Juan">
                    </div>
                    <div>
                        <label>Apellido</label>
                        <input type="text" id="pos-nc-apellido" class="form-input" required placeholder="Pérez">
                    </div>
                </div>
                <div class="form-group">
                    <label>Teléfono</label>
                    <input type="text" id="pos-nc-telefono" class="form-input" placeholder="9999-9999">
                </div>
                <div class="form-group">
                    <label>Correo Electrónico</label>
                    <input type="email" id="pos-nc-correo" class="form-input" placeholder="correo@ejemplo.com">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary btn-sm" onclick="posCloseNewClientModal()">Cancelar</button>
                <button type="submit" class="btn btn-primary btn-sm">Guardar Cliente</button>
            </div>
        </form>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: CATÁLOGO DE PRODUCTOS -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="pos-catalog-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title"><i class="fas fa-th" style="color:var(--primary)"></i> Catálogo de Productos</span>
            <button class="modal-close" onclick="posCloseCatalog()">&times;</button>
        </div>
        <div class="modal-body" style="padding:15px 24px;">
            <input type="text" id="pos-catalog-filter" class="form-input" placeholder="Buscar por nombre o código..." style="margin-bottom:15px;" oninput="posRenderCatalog()">
            <div style="max-height:350px; overflow-y:auto; border:1px solid var(--border-color); border-radius:var(--border-radius-md);">
                <table class="data-table" style="font-size:0.85rem;">
                    <thead>
                        <tr>
                            <th style="width:60px;">Foto</th>
                            <th>Código</th>
                            <th>Descripción</th>
                            <th>Precio</th>
                            <th>Stock</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody id="pos-catalog-table-body">
                        <!-- Loaded dynamically -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
// Logic specific to the POS Page
(function() {
    let clients = [];
    let products = [];
    let currentClient = { id_cliente: 1, nombre_cliente: 'CONSUMIDOR', apellido_cliente: 'FINAL' };
    let cart = [];

    window.initPOS = function() {
        cart = [];
        loadPaymentMethods();
        loadClients();
        loadProducts();
        renderCart();

        // Scan barcode focus and action
        const scanInput = document.getElementById('pos-scan-input');
        scanInput.focus();
        scanInput.addEventListener('keypress', e => {
            if (e.key === 'Enter') {
                const bc = scanInput.value.trim();
                if (bc) {
                    posSearchBarcode(bc);
                }
            }
        });

        // Payment method change extra fields handler
        const payMethod = document.getElementById('pos-pay-method');
        payMethod.addEventListener('change', () => {
            const opt = payMethod.options[payMethod.selectedIndex];
            const text = opt.text.toLowerCase();
            const extraFields = document.getElementById('pos-extra-fields');
            
            if (text.includes('tarjeta') || text.includes('transferencia')) {
                document.getElementById('pos-ref-label').textContent = text.includes('transferencia') ? 'Nº Referencia / ACH:' : 'Nº Voucher / Referencia:';
                extraFields.style.display = 'block';
            } else {
                extraFields.style.display = 'none';
                document.getElementById('pos-pay-ref').value = '';
                document.getElementById('pos-pay-bank').selectedIndex = 0;
            }
        });

        // Register client submit
        document.getElementById('pos-new-client-form').addEventListener('submit', e => {
            e.preventDefault();
            posSaveNewClient();
        });
    };

    function loadPaymentMethods() {
        fetch('controllers/pos.php?action=get_payment_methods')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    const sel = document.getElementById('pos-pay-method');
                    sel.innerHTML = '';
                    res.data.forEach(m => {
                        sel.insertAdjacentHTML('beforeend', `<option value="${m.id_metodo_pago}">${m.nombre_metodo}</option>`);
                    });
                }
            });
    }

    function loadClients() {
        fetch('controllers/clients.php?action=list&active=true')
            .then(r => r.json())
            .then(res => {
                if (res.success) clients = res.data;
            });
    }

    function loadProducts() {
        fetch('controllers/inventory.php?action=list')
            .then(r => r.json())
            .then(res => {
                if (res.success) products = res.data;
            });
    }

    // BARCODE SCANNING
    function posSearchBarcode(barcode) {
        fetch(`controllers/pos.php?action=search_product&barcode=${encodeURIComponent(barcode)}`)
            .then(r => r.json())
            .then(res => {
                const scanInput = document.getElementById('pos-scan-input');
                scanInput.value = '';
                scanInput.focus();

                if (res.success) {
                    addToCart(res.product);
                } else {
                    App.showToast(res.message, 'error');
                }
            })
            .catch(() => {
                App.showToast('Error de red al escanear.', 'error');
            });
    }

    // CART STATE ACTIONS
    function addToCart(prod) {
        // Buscar si ya está en el carrito
        const idx = cart.findIndex(item => item.id_producto === prod.id_producto);
        if (idx >= 0) {
            const nextQty = cart[idx].cantidad + 1;
            if (nextQty > prod.stock_producto) {
                App.showToast(`Stock insuficiente para '${prod.nombre_producto}'. Disponible: ${prod.stock_producto}`, 'error');
                return;
            }
            cart[idx].cantidad = nextQty;
            cart[idx].subtotal_fila = nextQty * cart[idx].precio;
        } else {
            if (prod.stock_producto < 1) {
                App.showToast(`'${prod.nombre_producto}' no tiene stock disponible.`, 'error');
                return;
            }
            cart.push({
                id_producto: prod.id_producto,
                nombre: prod.nombre_producto,
                precio: parseFloat(prod.precio_venta_producto),
                cantidad: 1,
                subtotal_fila: parseFloat(prod.precio_venta_producto),
                requiere_serie: prod.requiere_serie,
                stock_max: prod.stock_producto,
                ruta_imagen: prod.ruta_imagen_producto,
                imei: ''
            });
        }
        App.showToast('Producto agregado al carrito.');
        renderCart();
    }

    window.posRemoveCartItem = function(index) {
        cart.splice(index, 1);
        renderCart();
    };

    window.posUpdateQty = function(index, change) {
        const nextQty = cart[index].cantidad + change;
        if (nextQty <= 0) {
            posRemoveCartItem(index);
            return;
        }
        if (nextQty > cart[index].stock_max) {
            App.showToast(`Stock disponible máximo alcanzado (${cart[index].stock_max})`, 'error');
            return;
        }
        cart[index].cantidad = nextQty;
        cart[index].subtotal_fila = nextQty * cart[index].precio;
        renderCart();
    };

    window.posUpdateImeiVal = function(index, value) {
        cart[index].imei = value.trim();
    };

    window.posVerifyImeiUnique = function(index, field) {
        const value = field.value.trim();
        if (!value) return;

        // Verificar duplicados locales
        const duplicates = cart.filter((item, i) => i !== index && item.imei === value);
        if (duplicates.length > 0) {
            App.showToast('Este IMEI/Serie ya fue ingresado en el carrito.', 'error');
            field.style.borderColor = 'var(--danger)';
            return;
        }

        // Verificar duplicados en la base de datos
        fetch(`controllers/pos.php?action=check_imei&imei=${encodeURIComponent(value)}`)
            .then(r => r.json())
            .then(res => {
                if (res.success && res.exists) {
                    App.showToast(`El IMEI/Serie '${value}' ya fue vendido anteriormente.`, 'error');
                    field.style.borderColor = 'var(--danger)';
                } else {
                    field.style.borderColor = 'var(--border-color)';
                }
            });
    };

    function renderCart() {
        const tbody = document.getElementById('pos-cart-tbody');
        if (cart.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center" style="padding:40px; color:var(--text-light);">
                        <i class="fas fa-shopping-basket" style="font-size:3rem; display:block; margin-bottom:12px;"></i>
                        El carrito está vacío. Escanee un código o abra el Catálogo.
                    </td>
                </tr>
            `;
            recalculateTotals(0);
            return;
        }

        tbody.innerHTML = '';
        let sumSubtotal = 0;

        cart.forEach((item, idx) => {
            sumSubtotal += item.subtotal_fila;
            const img = item.ruta_imagen ? item.ruta_imagen : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
            
            // IMEI Input Row if required
            let imeiRow = '';
            if (item.requiere_serie) {
                imeiRow = `
                    <div class="imei-input-box">
                        <input type="text" class="imei-field" placeholder="Ingrese IMEI / Nº de Serie..." 
                               value="${item.imei}" onchange="posUpdateImeiVal(${idx}, this.value)" 
                               onblur="posVerifyImeiUnique(${idx}, this)" required>
                    </div>
                `;
            }

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td><img src="${img}" class="pos-item-img" onerror="this.src='https://cdn-icons-png.flaticon.com/512/869/869045.png'"></td>
                    <td>
                        <div style="font-weight:600;">${item.nombre}</div>
                        ${imeiRow}
                    </td>
                    <td>
                        <div class="pos-qty-ctrl">
                            <button class="pos-qty-btn" onclick="posUpdateQty(${idx}, -1)"><i class="fas fa-minus"></i></button>
                            <span class="pos-qty-val">${item.cantidad}</span>
                            <button class="pos-qty-btn" onclick="posUpdateQty(${idx}, 1)"><i class="fas fa-plus"></i></button>
                        </div>
                    </td>
                    <td style="text-align:right; font-weight:500;">L ${item.precio.toFixed(2)}</td>
                    <td style="text-align:right; font-weight:700;">L ${item.subtotal_fila.toFixed(2)}</td>
                    <td style="text-align:center;">
                        <button class="pos-item-remove" onclick="posRemoveCartItem(${idx})"><i class="fas fa-trash-alt"></i></button>
                    </td>
                </tr>
            `);
        });

        recalculateTotals(sumSubtotal);
    }

    function recalculateTotals(subtotal) {
        // En Honduras, las empresas pueden aplicar ISV del 15% incluido o agregado. Replicamos lógica del Swing
        const taxRate = 0.15;
        const tax = subtotal * taxRate;
        const total = subtotal + tax;

        document.getElementById('pos-subtotal-label').textContent = 'L ' + subtotal.toFixed(2);
        document.getElementById('pos-tax-label').textContent = 'L ' + tax.toFixed(2);
        document.getElementById('pos-total-label').textContent = 'L ' + total.toFixed(2);
    }

    // CHECKOUT PROCESS
    window.posProcessCheckout = function() {
        if (cart.length === 0) {
            App.showToast('El carrito está vacío.', 'error');
            return;
        }

        // Validar series vacías
        const missingImeis = cart.filter(item => item.requiere_serie && !item.imei);
        if (missingImeis.length > 0) {
            App.showToast(`Por favor ingrese el IMEI para todos los productos de serie.`, 'error');
            return;
        }

        // Verificar sesión de caja activa
        App.checkActiveCashSession((caja) => {
            // Subtotal, Impuesto y Total
            let subtotal = 0;
            cart.forEach(item => subtotal += item.subtotal_fila);
            const tax = subtotal * 0.15;
            const total = subtotal + tax;

            const idMetodo = document.getElementById('pos-pay-method').value;
            const ref = document.getElementById('pos-pay-ref').value;
            const bank = document.getElementById('pos-pay-bank').value;

            const fd = new FormData();
            fd.append('id_cliente', currentClient.id_cliente);
            fd.append('id_metodo_pago', idMetodo);
            fd.append('subtotal', subtotal.toFixed(4));
            fd.append('impuesto', tax.toFixed(4));
            fd.append('total', total.toFixed(4));
            fd.append('referencia_pago', ref);
            fd.append('banco_pago', bank);
            fd.append('detalles', JSON.stringify(cart));

            fetch('controllers/pos.php?action=process_sale', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    
                    // Abrir ticket en nueva pestaña para imprimir
                    const ticketUrl = `views/ticket.php?id_venta=${res.id_venta}`;
                    window.open(ticketUrl, '_blank', 'width=350,height=600');

                    // Reiniciar POS
                    cart = [];
                    currentClient = { id_cliente: 1, nombre_cliente: 'CONSUMIDOR', apellido_cliente: 'FINAL' };
                    document.getElementById('pos-selected-client-name').textContent = 'CONSUMIDOR FINAL';
                    document.getElementById('pos-pay-ref').value = '';
                    document.getElementById('pos-pay-bank').selectedIndex = 0;
                    document.getElementById('pos-extra-fields').style.display = 'none';
                    renderCart();
                } else {
                    alert(res.message);
                }
            })
            .catch(err => {
                alert('Error al registrar la venta.');
            });
        });
    };


    // CLIENT DIALOGS AND LOGIC
    window.posOpenClientSearch = function() {
        document.getElementById('pos-client-modal').style.display = 'flex';
        document.getElementById('pos-client-filter').value = '';
        posRenderClientList();
    };

    window.posCloseClientSearch = function() {
        document.getElementById('pos-client-modal').style.display = 'none';
    };

    window.posRenderClientList = function() {
        const filter = document.getElementById('pos-client-filter').value.toLowerCase();
        const tbody = document.getElementById('pos-client-table-body');
        tbody.innerHTML = '';

        const filtered = clients.filter(c => {
            const name = (c.nombre_cliente + ' ' + (c.apellido_cliente || '')).toLowerCase();
            return name.includes(filter) || c.identidad_cliente.includes(filter);
        });

        filtered.forEach(c => {
            const fullName = c.nombre_cliente + ' ' + (c.apellido_cliente || '');
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td>${c.identidad_cliente}</td>
                    <td>${fullName}</td>
                    <td class="text-right">
                        <button class="btn btn-secondary btn-sm" onclick="posSelectClient(${c.id_cliente}, '${fullName}')">Seleccionar</button>
                    </td>
                </tr>
            `);
        });
    };

    window.posSelectClient = function(id, name) {
        currentClient = { id_cliente: id };
        document.getElementById('pos-selected-client-name').textContent = name;
        posCloseClientSearch();
    };

    window.posOpenNewClientModal = function() {
        document.getElementById('pos-new-client-modal').style.display = 'flex';
        document.getElementById('pos-nc-identidad').value = '';
        document.getElementById('pos-nc-nombre').value = '';
        document.getElementById('pos-nc-apellido').value = '';
        document.getElementById('pos-nc-telefono').value = '';
        document.getElementById('pos-nc-correo').value = '';
    };

    window.posCloseNewClientModal = function() {
        document.getElementById('pos-new-client-modal').style.display = 'none';
    };

    function posSaveNewClient() {
        const iden = document.getElementById('pos-nc-identidad').value;
        const nom = document.getElementById('pos-nc-nombre').value;
        const ape = document.getElementById('pos-nc-apellido').value;
        const tel = document.getElementById('pos-nc-telefono').value;
        const corr = document.getElementById('pos-nc-correo').value;

        const fd = new FormData();
        fd.append('identidad_cliente', iden);
        fd.append('nombre_cliente', nom);
        fd.append('apellido_cliente', ape);
        fd.append('telefono_cliente', tel);
        fd.append('correo_cliente', corr);

        fetch('controllers/clients.php?action=save', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadClients(); // Recargar listado en memoria
                posSelectClient(res.id_cliente || 9999, nom + ' ' + ape); // Seleccionar temporalmente
                posCloseNewClientModal();
            } else {
                alert(res.message);
            }
        });
    }

    // CATALOG LIST LOGIC
    window.posOpenCatalog = function() {
        document.getElementById('pos-catalog-modal').style.display = 'flex';
        document.getElementById('pos-catalog-filter').value = '';
        posRenderCatalog();
    };

    window.posCloseCatalog = function() {
        document.getElementById('pos-catalog-modal').style.display = 'none';
    };

    window.posRenderCatalog = function() {
        const filter = document.getElementById('pos-catalog-filter').value.toLowerCase();
        const tbody = document.getElementById('pos-catalog-table-body');
        tbody.innerHTML = '';

        const filtered = products.filter(p => {
            return p.nombre_producto.toLowerCase().includes(filter) || 
                   (p.codigo_barras_producto && p.codigo_barras_producto.toLowerCase().includes(filter));
        });

        filtered.forEach(p => {
            const img = p.ruta_imagen_producto ? p.ruta_imagen_producto : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td><img src="${img}" style="width:36px; height:36px; object-fit:cover; border-radius:4px; cursor:pointer;" onclick="App.showImagePreview('${img}')" onerror="this.src='https://cdn-icons-png.flaticon.com/512/869/869045.png'"></td>
                    <td>${p.codigo_barras_producto || 'N/A'}</td>
                    <td>${p.nombre_producto}</td>
                    <td class="bold">L ${parseFloat(p.precio_venta_producto).toFixed(2)}</td>
                    <td>
                        <span class="badge ${p.stock_producto > p.stock_minimo_producto ? 'badge-success' : 'badge-danger'}">
                            ${p.stock_producto}
                        </span>
                    </td>
                    <td class="text-right">
                        <button class="btn btn-primary btn-sm" onclick="posAddFromCatalog(${p.id_producto})">Agregar</button>
                    </td>
                </tr>
            `);
        });
    };

    window.posAddFromCatalog = function(id) {
        const p = products.find(prod => prod.id_producto == id);
        if (p) {
            addToCart(p);
            posCloseCatalog();
        }
    };
})();
</script>
