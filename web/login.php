<?php
session_start();
if (isset($_SESSION['user_id'])) {
    header('Location: index.php');
    exit;
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ORION SYSTEMS - Iniciar Sesión</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Outfit:wght@600;700;800&display=swap');
        
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }
        
        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            overflow: hidden;
            position: relative;
        }

        /* Fondo Decorativo */
        body::before, body::after {
            content: '';
            position: absolute;
            width: 300px;
            height: 300px;
            border-radius: 50%;
            z-index: 1;
            filter: blur(80px);
            opacity: 0.15;
        }

        body::before {
            background-color: #27ae60;
            top: 10%;
            left: 15%;
        }

        body::after {
            background-color: #e3000f;
            bottom: 10%;
            right: 15%;
        }
        
        /* Glassmorphism Card */
        .login-card {
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid rgba(255, 255, 255, 0.08);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border-radius: 24px;
            padding: 40px;
            width: 100%;
            max-width: 420px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
            z-index: 10;
            animation: cardFade 0.6s cubic-bezier(0.4, 0, 0.2, 1);
        }

        @keyframes cardFade {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .login-header {
            text-align: center;
            margin-bottom: 35px;
        }
        
        .login-logo {
            font-family: 'Outfit', sans-serif;
            font-size: 2rem;
            font-weight: 800;
            color: #fff;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .login-logo span {
            color: #e3000f; /* Rojo */
        }
        
        .login-subtitle {
            font-size: 0.85rem;
            color: #94a3b8;
            font-weight: 500;
            letter-spacing: 0.5px;
        }
        
        .form-group {
            margin-bottom: 22px;
            position: relative;
        }
        
        .form-group label {
            display: block;
            font-size: 0.75rem;
            font-weight: 600;
            color: #94a3b8;
            margin-bottom: 8px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .input-wrapper {
            position: relative;
        }

        .input-wrapper i {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            color: #64748b;
            font-size: 1rem;
            transition: 0.2s;
        }

        .login-input {
            width: 100%;
            height: 48px;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 12px;
            padding: 0 20px 0 45px;
            color: #fff;
            font-size: 0.95rem;
            font-family: inherit;
            outline: none;
            transition: all 0.2s;
        }
        
        .login-input:focus {
            border-color: #27ae60; /* Verde */
            background: rgba(255, 255, 255, 0.08);
            box-shadow: 0 0 0 3px rgba(39, 174, 96, 0.15);
        }

        .login-input:focus + i {
            color: #27ae60;
        }
        
        .btn-login {
            width: 100%;
            height: 50px;
            background: #27ae60;
            color: #fff;
            border: none;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 700;
            cursor: pointer;
            transition: background 0.2s;
            margin-top: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
        }
        
        .btn-login:hover {
            background: #219653;
        }

        .alert-box {
            background: rgba(227, 0, 15, 0.15);
            border: 1px solid rgba(227, 0, 15, 0.25);
            border-radius: 12px;
            padding: 12px 16px;
            color: #fca5a5;
            font-size: 0.85rem;
            margin-bottom: 20px;
            display: none;
            align-items: center;
            gap: 10px;
            animation: shake 0.4s ease;
        }

        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-6px); }
            75% { transform: translateX(6px); }
        }
    </style>
</head>
<body>

    <div class="login-card">
        <div class="login-header">
            <div class="login-logo">ORION<span>SYS</span></div>
            <div class="login-subtitle">CONEXION A TU ALCANCE</div>
        </div>

        <div class="alert-box" id="error-alert">
            <i class="fas fa-exclamation-circle"></i>
            <span id="error-message">Error de credenciales</span>
        </div>

        <form id="login-form">
            <div class="form-group">
                <label for="username">Usuario</label>
                <div class="input-wrapper">
                    <input type="text" id="username" class="login-input" placeholder="Nombre de usuario" required autocomplete="off">
                    <i class="fas fa-user"></i>
                </div>
            </div>

            <div class="form-group">
                <label for="password">Contraseña</label>
                <div class="input-wrapper">
                    <input type="password" id="password" class="login-input" placeholder="••••••••" required>
                    <i class="fas fa-lock"></i>
                </div>
            </div>

            <button type="submit" class="btn-login" id="submit-btn">
                <span>Ingresar</span>
                <i class="fas fa-arrow-right"></i>
            </button>
        </form>
    </div>

    <script>
        document.getElementById('login-form').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const submitBtn = document.getElementById('submit-btn');
            const alertBox = document.getElementById('error-alert');
            const alertMsg = document.getElementById('error-message');
            
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span>Verificando...</span> <i class="fas fa-spinner fa-spin"></i>';
            alertBox.style.display = 'none';

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            const formData = new FormData();
            formData.append('username', username);
            formData.append('password', password);

            fetch('controllers/auth.php?action=login', {
                method: 'POST',
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    window.location.href = 'index.php';
                } else {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<span>Ingresar</span> <i class="fas fa-arrow-right"></i>';
                    
                    alertMsg.textContent = data.message;
                    alertBox.style.display = 'flex';
                }
            })
            .catch(err => {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<span>Ingresar</span> <i class="fas fa-arrow-right"></i>';
                
                alertMsg.textContent = 'Error de conexión con el servidor.';
                alertBox.style.display = 'flex';
            });
        });
    </script>
</body>
</html>
