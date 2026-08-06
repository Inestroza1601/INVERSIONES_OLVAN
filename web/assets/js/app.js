// Global System State
const App = {
    currentView: (typeof CURRENT_ROLE_ID !== 'undefined' && CURRENT_ROLE_ID == 3) ? 'pos' : 'dashboard',
    cart: [],
    paymentMethods: [],
    companyInfo: null,

    init() {
        this.bindNavigation();
        this.loadView((typeof CURRENT_ROLE_ID !== 'undefined' && CURRENT_ROLE_ID == 3) ? 'pos' : 'dashboard');
        this.startClock();
        this.fetchCompanyInfo();
    },

    startClock() {
        const datetimeEl = document.getElementById('datetime-display');
        if (!datetimeEl) return;
        const update = () => {
            const now = new Date();
            datetimeEl.textContent = now.toLocaleDateString('es-HN', {
                weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
            }) + ' | ' + now.toLocaleTimeString('es-HN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        };
        update();
        setInterval(update, 1000);
    },

    fetchCompanyInfo() {
        fetch('controllers/company.php?action=get')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    this.companyInfo = res.data;
                    document.title = this.companyInfo.nombre_empresa + " - Web Portal";
                }
            });
    },

    bindNavigation() {
        document.querySelectorAll('.sidebar-menu a').forEach(link => {
            link.addEventListener('click', e => {
                e.preventDefault();
                const view = link.getAttribute('data-view');
                if (view) {
                    this.loadView(view);
                }
            });
        });

        // Logout
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.logout());
        }
    },

    loadView(viewName) {
        // Restricción de permisos para rol Cajero (id_rol = 3)
        const allowedViewsForCashier = ['pos', 'sales_history', 'layaways', 'inventory', 'clients', 'warranties', 'cash_control'];
        if (typeof CURRENT_ROLE_ID !== 'undefined' && CURRENT_ROLE_ID == 3 && !allowedViewsForCashier.includes(viewName)) {
            App.showToast('Acceso Denegado: No cuenta con permisos para ver esta sección.', 'error');
            return;
        }

        this.currentView = viewName;
        
        // Active Class in Sidebar
        document.querySelectorAll('.sidebar-menu li').forEach(li => li.classList.remove('active'));
        const activeLink = document.querySelector(`.sidebar-menu a[data-view="${viewName}"]`);
        if (activeLink) {
            activeLink.closest('li').classList.add('active');
            const pageTitle = activeLink.querySelector('span').textContent;
            document.getElementById('header-page-title').textContent = pageTitle;
        }

        // Fetch View content
        const contentArea = document.getElementById('content-area');
        contentArea.style.opacity = 0;

        fetch(`views/${viewName}.php`)
            .then(response => {
                if (!response.ok) throw new Error('Vista no encontrada');
                return response.text();
            })
            .then(html => {
                contentArea.innerHTML = html;
                
                // Re-ejecutar scripts inyectados para que el navegador los interprete
                const scripts = contentArea.querySelectorAll('script');
                scripts.forEach(oldScript => {
                    const newScript = document.createElement('script');
                    Array.from(oldScript.attributes).forEach(attr => newScript.setAttribute(attr.name, attr.value));
                    newScript.appendChild(document.createTextNode(oldScript.innerHTML));
                    oldScript.parentNode.replaceChild(newScript, oldScript);
                });

                contentArea.style.opacity = 1;
                this.initViewLogic(viewName);
            })
            .catch(err => {
                contentArea.innerHTML = `<div class="card"><h3 style="color:var(--danger)">Error al cargar la vista</h3><p>${err.message}</p></div>`;
                contentArea.style.opacity = 1;
            });
    },

    initViewLogic(viewName) {
        switch (viewName) {
            case 'dashboard':
                if (window.initDashboard) window.initDashboard();
                break;
            case 'pos':
                if (window.initPOS) window.initPOS();
                break;
            case 'inventory':
                if (window.initInventory) window.initInventory();
                break;
            case 'clients':
                if (window.initClients) window.initClients();
                break;
            case 'warranties':
                if (window.initWarranties) window.initWarranties();
                break;
            case 'sales_history':
                if (window.initSalesHistory) window.initSalesHistory();
                break;
            case 'layaways':
                if (window.initLayaways) window.initLayaways();
                break;
            case 'cash_control':
                if (window.initCashControl) window.initCashControl();
                break;
            case 'stats':
                if (window.initStats) window.initStats();
                break;
            case 'users':
                if (window.initUsers) window.initUsers();
                break;
            case 'settings':
                if (window.initSettings) window.initSettings();
                break;
        }
    },

    logout() {
        App.confirm('¿Seguro que desea cerrar sesión?', () => {
            fetch('controllers/auth.php?action=logout')
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        window.location.href = 'login.php';
                    }
                });
        });
    },

    // UTILITIES: TOASTS
    showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `toast ${type === 'error' ? 'error' : ''}`;
        
        const icon = type === 'error' ? 'fa-exclamation-circle' : 'fa-check-circle';
        toast.innerHTML = `
            <i class="fas ${icon}" style="color:${type === 'error' ? 'var(--danger)' : 'var(--primary)'}; font-size:1.2rem;"></i>
            <span class="toast-msg">${message}</span>
        `;
        
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.style.animation = 'fadeIn 0.2s reverse forwards';
            setTimeout(() => toast.remove(), 200);
        }, 3000);
    },

    // UTILITIES: CONFIRMATION BY DIGITAL PASSWORD SIGNATURE
    askSignature(callback) {
        // Crear modal de firma
        const modalHtml = `
            <div class="modal-backdrop" id="signature-modal">
                <div class="modal-content" style="max-width: 380px;">
                    <div class="modal-header">
                        <span class="modal-title"><i class="fas fa-signature" style="color:var(--primary)"></i> Autorización Requerida</span>
                        <button class="modal-close" onclick="document.getElementById('signature-modal').remove()">&times;</button>
                    </div>
                    <form id="signature-form">
                        <div class="modal-body">
                            <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:15px;">
                                Por favor, ingrese su contraseña para firmar y autorizar este movimiento de inventario.
                            </p>
                            <div class="form-group">
                                <label>Contraseña de Autorización</label>
                                <input type="password" id="signature-password" class="form-input" required placeholder="••••••••">
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary btn-sm" onclick="document.getElementById('signature-modal').remove()">Cancelar</button>
                            <button type="submit" class="btn btn-primary btn-sm">Confirmar Firma</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        document.getElementById('signature-password').focus();

        document.getElementById('signature-form').addEventListener('submit', e => {
            e.preventDefault();
            const password = document.getElementById('signature-password').value;

            const fd = new FormData();
            fd.append('password', password);

            fetch('controllers/auth.php?action=verify_signature', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    document.getElementById('signature-modal').remove();
                    callback(res.user_id, res.username);
                } else {
                    alert(res.message);
                }
            })
            .catch(err => {
                alert('Error de conexión al firmar.');
            });
        });
    },

    checkActiveCashSession(callback) {
        fetch('controllers/cash_control.php?action=get_active')
            .then(r => r.json())
            .then(res => {
                if (res.success && res.has_active) {
                    callback(res.data);
                } else {
                    App.showCashOpeningModal(callback);
                }
            })
            .catch(err => {
                App.showToast('Error al validar estado de caja.', 'error');
            });
    },

    showCashOpeningModal(callback) {
        const existing = document.getElementById('cash-open-forced-modal');
        if (existing) existing.remove();

        let cashierInputHtml = '';
        if (typeof CURRENT_ROLE_ID !== 'undefined' && CURRENT_ROLE_ID != 3) {
            cashierInputHtml = `
                <div class="form-group" style="margin-top:12px;">
                    <label>Nombre del Cajero Asignado</label>
                    <input type="text" id="cash-open-forced-cajero" class="form-input" placeholder="Nombre de quien operará la caja..." required>
                </div>
            `;
        }

        const cancelBtnHtml = (typeof CURRENT_ROLE_ID !== 'undefined' && CURRENT_ROLE_ID == 3)
            ? `<button type="button" class="btn btn-secondary btn-sm" onclick="App.logout()">Cerrar Sesión</button>`
            : `<button type="button" class="btn btn-secondary btn-sm" onclick="App.loadView('dashboard'); document.getElementById('cash-open-forced-modal').remove();">Ir al Dashboard</button>`;

        const modalHtml = `
            <div class="modal-backdrop" id="cash-open-forced-modal" style="display:flex; z-index:99999;">
                <div class="modal-content" style="max-width:380px;">
                    <div class="modal-header">
                        <span class="modal-title"><i class="fas fa-cash-register" style="color:var(--primary)"></i> Apertura de Caja Requerida</span>
                    </div>
                    <form id="cash-open-forced-form">
                        <div class="modal-body">
                            <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:15px;">
                                Para registrar cobros, ventas o abonos, debe abrir el turno de caja e ingresar el dinero en efectivo inicial de la gaveta.
                            </p>
                            <div class="form-group">
                                <label>Monto de Apertura (Efectivo Inicial en L)</label>
                                <input type="number" step="0.01" min="0" id="cash-open-forced-monto" class="form-input" required placeholder="L 0.00" value="0.00">
                            </div>
                            ${cashierInputHtml}
                        </div>
                        <div class="modal-footer">
                            ${cancelBtnHtml}
                            <button type="submit" class="btn btn-primary btn-sm">Abrir Turno</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        document.getElementById('cash-open-forced-monto').focus();
        document.getElementById('cash-open-forced-monto').select();

        document.getElementById('cash-open-forced-form').addEventListener('submit', e => {
            e.preventDefault();
            const monto = parseFloat(document.getElementById('cash-open-forced-monto').value) || 0;
            let cajero = (typeof CURRENT_USERNAME !== 'undefined') ? CURRENT_USERNAME : '';
            
            const cajeroEl = document.getElementById('cash-open-forced-cajero');
            if (cajeroEl) {
                cajero = cajeroEl.value.trim();
            }

            const fd = new FormData();
            fd.append('monto_apertura', monto);
            fd.append('cajero_turno', cajero);

            fetch('controllers/cash_control.php?action=open', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    document.getElementById('cash-open-forced-modal').remove();
                    App.showToast(res.message);
                    App.checkActiveCashSession(callback);
                } else {
                    alert(res.message);
                }
            })
            .catch(err => {
                alert('Error de conexión al abrir caja.');
            });
        });
    },

    showImagePreview(src) {
        const existing = document.getElementById('image-preview-modal');
        if (existing) existing.remove();

        const images = src ? src.split('|') : [];
        if (images.length === 0) return;
        
        let currentIndex = 0;

        const keydownHandler = (e) => {
            if (e.key === 'ArrowLeft') {
                currentIndex = (currentIndex > 0) ? currentIndex - 1 : images.length - 1;
                initModal();
            } else if (e.key === 'ArrowRight') {
                currentIndex = (currentIndex < images.length - 1) ? currentIndex + 1 : 0;
                initModal();
            } else if (e.key === 'Escape') {
                closeModal();
            }
        };

        const closeModal = () => {
            const el = document.getElementById('image-preview-modal');
            if (el) el.remove();
            document.removeEventListener('keydown', keydownHandler);
        };

        const renderModalContent = () => {
            const currentImg = images[currentIndex];
            let navHtml = '';
            let thumbnailsHtml = '';
            if (images.length > 1) {
                navHtml = `
                    <div style="display:flex; justify-content:space-between; width:100%; position:absolute; top:50%; transform:translateY(-50%); pointer-events:none; padding:0 10px; box-sizing:border-box;">
                        <button id="prev-img-preview" style="pointer-events:auto; background:rgba(0,0,0,0.6); color:#fff; border:none; border-radius:50%; width:44px; height:44px; font-size:1.5rem; cursor:pointer; display:flex; align-items:center; justify-content:center; box-shadow:0 2px 10px rgba(0,0,0,0.3); transition:background 0.2s;"><i class="fas fa-chevron-left"></i></button>
                        <button id="next-img-preview" style="pointer-events:auto; background:rgba(0,0,0,0.6); color:#fff; border:none; border-radius:50%; width:44px; height:44px; font-size:1.5rem; cursor:pointer; display:flex; align-items:center; justify-content:center; box-shadow:0 2px 10px rgba(0,0,0,0.3); transition:background 0.2s;"><i class="fas fa-chevron-right"></i></button>
                    </div>
                    <div style="color:#fff; margin-top:10px; font-weight:600; text-shadow:0 2px 5px rgba(0,0,0,0.5); font-size:0.9rem;">
                        ${currentIndex + 1} de ${images.length}
                    </div>
                `;

                thumbnailsHtml = `
                    <div style="display:flex; gap:8px; margin-top:15px; justify-content:center; flex-wrap:wrap; pointer-events:auto;">
                        ${images.map((img, idx) => `
                            <img src="${img}" class="preview-thumb-item" data-index="${idx}" style="width:48px; height:48px; object-fit:contain; background:#f8fafc; border-radius:4px; border:2px solid ${idx === currentIndex ? '#27ae60' : 'rgba(255,255,255,0.3)'}; cursor:pointer; opacity:${idx === currentIndex ? '1' : '0.6'}; transition:all 0.2s;" onmouseover="this.style.opacity='1'" onmouseout="if(${idx} !== currentIndex) this.style.opacity='0.6'">
                        `).join('')}
                    </div>
                `;
            }

            return `
                <div class="modal-backdrop" id="image-preview-modal" style="display:flex; z-index:999999; background:rgba(0,0,0,0.85); align-items:center; justify-content:center;" onclick="if(event.target.id === 'image-preview-modal') window.App.closeImagePreviewModal()">
                    <div style="position:relative; max-width:90%; max-height:90%; display:flex; flex-direction:column; align-items:center; width: 600px;">
                        <img src="${currentImg}" style="max-width:90vw; max-height:72vh; width:100%; object-fit:contain; border-radius:8px; border:4px solid #fff; box-shadow:0 10px 30px rgba(0,0,0,0.5);">
                        <div style="position:absolute; top:-35px; right:0; color:#fff; font-size:2rem; cursor:pointer; font-weight:bold; line-height:1;" onclick="window.App.closeImagePreviewModal()">&times;</div>
                        ${navHtml}
                        ${thumbnailsHtml}
                    </div>
                </div>
            `;
        };

        const initModal = () => {
            const old = document.getElementById('image-preview-modal');
            if (old) old.remove();

            const temp = document.createElement('div');
            temp.innerHTML = renderModalContent();
            const modalEl = temp.firstElementChild;
            document.body.appendChild(modalEl);

            if (images.length > 1) {
                document.getElementById('prev-img-preview').onclick = (e) => {
                    e.stopPropagation();
                    currentIndex = (currentIndex > 0) ? currentIndex - 1 : images.length - 1;
                    initModal();
                };
                document.getElementById('next-img-preview').onclick = (e) => {
                    e.stopPropagation();
                    currentIndex = (currentIndex < images.length - 1) ? currentIndex + 1 : 0;
                    initModal();
                };

                const thumbs = document.querySelectorAll('.preview-thumb-item');
                thumbs.forEach(thumb => {
                    thumb.onclick = (e) => {
                        e.stopPropagation();
                        currentIndex = parseInt(thumb.getAttribute('data-index'));
                        initModal();
                    };
                });
            }
        };

        // Export to global scope
        window.App.closeImagePreviewModal = closeModal;

        // Add keyboard listener
        document.addEventListener('keydown', keydownHandler);

        initModal();
    },

    confirm(message, callbackOnConfirm, callbackOnCancel = null) {
        const existing = document.getElementById('system-confirm-modal');
        if (existing) existing.remove();

        const modalHtml = `
            <div class="modal-backdrop" id="system-confirm-modal" style="display:flex; z-index:9999999; background:rgba(0,0,0,0.6); align-items:center; justify-content:center;">
                <div class="modal-content animate-fade-in" style="max-width:380px; padding:25px; text-align:center; border-radius:var(--border-radius-lg); box-shadow:var(--shadow-lg);">
                    <div style="font-size:3rem; color:var(--primary); margin-bottom:15px; animation: pulse 1.5s infinite;">
                        <i class="fas fa-question-circle"></i>
                    </div>
                    <h3 style="margin-bottom:10px; font-size:1.15rem; font-weight:700;">Confirmar Acción</h3>
                    <p style="font-size:0.9rem; color:var(--text-muted); line-height:1.45; margin-bottom:24px;">
                        ${message}
                    </p>
                    <div style="display:flex; gap:12px; justify-content:center;">
                        <button type="button" class="btn btn-secondary btn-sm" id="sys-confirm-btn-cancel" style="width:110px; height:36px;">Cancelar</button>
                        <button type="button" class="btn btn-primary btn-sm" id="sys-confirm-btn-ok" style="width:110px; height:36px; background:var(--primary);">Aceptar</button>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        const modal = document.getElementById('system-confirm-modal');
        const btnOk = document.getElementById('sys-confirm-btn-ok');
        const btnCancel = document.getElementById('sys-confirm-btn-cancel');

        btnOk.addEventListener('click', () => {
            modal.remove();
            if (callbackOnConfirm) callbackOnConfirm();
        });

        btnCancel.addEventListener('click', () => {
            modal.remove();
            if (callbackOnCancel) callbackOnCancel();
        });
    }
};

// Start App when loaded
document.addEventListener('DOMContentLoaded', () => App.init());
