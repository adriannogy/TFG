import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import 'leaflet/dist/leaflet.css';
import { useEffect, useRef, useState } from 'react';
import {
  FaArrowLeft,
  FaBars,
  FaBookmark,
  FaCheck,
  FaHeart,
  FaHome,
  FaImage,
  FaLock,
  FaMapMarkerAlt,
  FaRegBookmark,
  FaSearch,
  FaSignOutAlt,
  FaStar,
  FaTimes,
  FaUser,
  FaUserEdit,
  FaUserFriends,
  FaUserPlus,
  FaUserSlash,
  FaUtensils
} from 'react-icons/fa';
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet';
import './App.css';
let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconAnchor: [12, 41],
    popupAnchor: [1, -34]
});

L.Marker.prototype.options.icon = DefaultIcon;

function App() {
  
  // --- 1. ESTADOS DE LA APLICACION ---
  const [view, setView] = useState('welcome');
  const [error, setError] = useState('');
  const [token, setToken] = useState('');
  
  // Estados para formularios
  const [nombreUsuario, setNombreUsuario] = useState('');
  const [email, setEmail] = useState('');
  const [pwd, setPwd] = useState('');
  const [confirmPwd, setConfirmPwd] = useState('');
  const [oldPwd, setOldPwd] = useState('');
  const [confirmEmail, setConfirmEmail] = useState('');

  // Estados de datos
  const [profile, setProfile] = useState(null);
  const [viewedProfile, setViewedProfile] = useState(null);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [userList, setUserList] = useState([]);
  const [listTitle, setListTitle] = useState('');
  
  // Estados de busqueda de usuarios
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  
  // Estados de busqueda de restaurantes
  const [filterNombre, setFilterNombre] = useState('');
  const [filterCiudad, setFilterCiudad] = useState('');
  const [filterTipoCocina, setFilterTipoCocina] = useState('');
  const [filterDireccion, setFilterDireccion] = useState('');
  const [restaurantResults, setRestaurantResults] = useState(null);
  const [searchPage, setSearchPage] = useState(0);

  const [favoritedRestaurantsMap, setFavoritedRestaurantsMap] = useState(new Map());
  const [favoritesListResults, setFavoritesListResults] = useState(null);
  const [favoritesPage, setFavoritesPage] = useState(0);
  const [previousView, setPreviousView] = useState('home');
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);

  const [reviewRating, setReviewRating] = useState(0);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewPhotos, setReviewPhotos] = useState([]);
  const [selectedReview, setSelectedReview] = useState(null);

  const [feedResults, setFeedResults] = useState(null);
  const [feedPage, setFeedPage] = useState(0);

  // Estado para guardar la foto seleccionada antes de subirla
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);

  // Refs y Modales
  const fileInputRef = useRef(null);
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [modalImageUrl, setModalImageUrl] = useState('');


  const commonCuisines = [
    { value: '', label: 'Cualquier tipo de cocina' },
    { value: 'spanish', label: 'Española' },
    { value: 'tapas', label: 'Tapas' },
    { value: 'basque', label: 'Vasca' },
    { value: 'galician', label: 'Gallega' },
    { value: 'mediterranean', label: 'Mediterránea' },
    { value: 'italian', label: 'Italiana' },
    { value: 'pizza', label: 'Pizza' },
    { value: 'japanese', label: 'Japonesa' },
    { value: 'sushi', label: 'Sushi' },
    { value: 'chinese', label: 'China' },
    { value: 'mexican', label: 'Mexicana' },
    { value: 'indian', label: 'India' },
    { value: 'burger', label: 'Hamburguesa' },
    { value: 'fast_food', label: 'Comida rápida' },
    { value: 'seafood', label: 'Marisco' },
    { value: 'vegan', label: 'Vegana' },
    { value: 'vegetarian', label: 'Vegetariana' },
  ];
  // --- 2. HOOKS DE EFECTO (useEffect) ---

  // Hook para leer el token de reseteo de la URL
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const tokenFromUrl = params.get('token');
    if (window.location.pathname === '/reset-password' && tokenFromUrl) {
      console.log("Token detectado:", tokenFromUrl);
      setToken(tokenFromUrl);
      setView('restablecer');
    }
  }, []);

  // Hook para la busqueda de usuarios
  useEffect(() => {
    if (view !== 'search' || searchQuery.trim().length < 2) {
      setSearchResults([]);
      return;
    }
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/usuarios/buscar?q=${searchQuery}`;
    const searchTimer = setTimeout(() => {
      fetch(url, { headers: { 'Authorization': `Bearer ${token}` } })
        .then(response => response.json())
        .then(data => setSearchResults(data))
        .catch(err => console.error("Error buscando usuarios:", err));
    }, 300);
    return () => clearTimeout(searchTimer);
  }, [searchQuery, view]);

  useEffect(() => {
    if (view === 'editarPerfil' && profile) {
      console.log("Cargando datos del perfil al formulario:", profile); 
      
      setNombreUsuario(profile.nombreUsuario || '');
      setEmail(profile.email || '');
      
      setOldPwd('');
      setPwd('');
      setConfirmPwd('');
      setError('');
    }
  }, [view, profile]); 

  
  

  // --- 3. MANEJADORES DE VISTAS ---
  const handleContinue = () => setView('opciones');
  const handleShowLogin = () => { setView('iniciar sesión'); setError(''); };
  const handleShowRegister = () => { setView('registro'); setError(''); };
  const handleShowForgotPassword = () => { setView('recuperar'); setError(''); };
  const handleShowMenu = () => setView('menu');
  const handleShowSearch = () => { setView('search'); setError(''); };
  const handleShowRestauranteSearch = () => { setView('filterRestaurantes'); setError(''); };
  const handleShowRestaurantResults = () => { setView('restaurantResults'); setError(''); };
  
  const handleShowEditProfile = () => {
    setNombreUsuario(profile.nombreUsuario || '');

    setEmail(profile.email || '');
    
    setConfirmEmail(profile.email || ''); 

    setOldPwd('');
    setPwd('');
    setConfirmPwd('');
    
    setSelectedFile(null);
    setPreviewUrl(null);
    setError('');

    setView('editarPerfil');
  };

  const handleShowRequests = () => {
    console.log("Cargando solicitudes pendientes...");
    setError('');
    const token = localStorage.getItem('token');
    const url = 'http://localhost:8080/api/relaciones/solicitudes/pendientes';

    fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('No se pudieron cargar las solicitudes.');
      }
      return response.json();
    })
    .then(data => {
      setPendingRequests(data);
      setView('requests');   
    })
    .catch(error => {
      console.error('Error al cargar solicitudes:', error);
      setError(error.message);
    });
  };

  const handleShowRestaurantDetail = (restaurante) => {
    setSelectedRestaurant(restaurante); 
    setPreviousView('restaurantResults')
    setView('restaurantDetail');      
  };

  const handleBackToPreviousView = () => {
    setView(previousView);
  };

  const handleShowRestaurantDetailFromFavorites = (localRestaurante) => {
    const simulatedExternalObject = {
      id: localRestaurante.osmId,
      lat: localRestaurante.lat,
      lon: localRestaurante.lon,
      tags: {
        'name': localRestaurante.nombre,
        'addr:city': localRestaurante.ciudad,
        'addr:street': localRestaurante.direccion,
        'cuisine': localRestaurante.tipoCocina
      }
    };
    handleShowRestaurantDetail(simulatedExternalObject);
    setPreviousView('favoritesList');
    setView('restaurantDetail');
  };

  const handleBackToHome = () => {
    setView('home');
    const token = localStorage.getItem('token');
    if (token) fetchProfileData(token);
  };
  
  const handleBack = () => {
    setView('opciones');
    setNombreUsuario(''); setEmail(''); setPwd(''); setConfirmPwd(''); setError('');
  };
  
  

  const handleShowUserProfile = (username) => {
    console.log("Mostrando perfil de:", username);
    setError('');
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/usuarios/${username}/perfil`;
  
    fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('No se pudo cargar el perfil de este usuario.');
      }
      return response.json();
    })
    .then(data => {
      setViewedProfile(data);
      setView('userProfile');
    })
    .catch(error => {
      console.error("Error al cargar perfil:", error);
      setError(error.message);
      setView('search'); 
    });
  };

  const handleShowImageModal = (imageUrl) => {
    if (imageUrl) {
      setModalImageUrl(imageUrl);
      setIsImageModalOpen(true);
    }
  };

  const handleCloseImageModal = () => {
    setIsImageModalOpen(false);
    setModalImageUrl('');
  };


  // --- 4. MANEJADORES DE ACCIONES ---

  const fetchProfileData = (authToken) => {
    console.log("Buscando datos del perfil...");
    const profileUrl = 'http://localhost:8080/api/perfil/me';
    fetch(profileUrl, {
      method: 'GET',
      headers: { 'Authorization': `Bearer ${authToken}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudieron cargar los datos del perfil.');
      return response.json();
    })
    .then(data => {
      console.log("Perfil recibido:", data);
      setProfile(data);
    })
    .catch(error => {
      console.error('Error al cargar perfil:', error);
      setError('Error al cargar tu perfil.');
    });
  };

  const handleUnifiedSave = async (event) => {
    event.preventDefault();
    setError('');
    const token = localStorage.getItem('token');
    let messages = [];

    const safeNombre = nombreUsuario || '';
    const safeEmail = email || '';
    const safePwd = pwd || '';
    const safeConfirmPwd = confirmPwd || '';
    const safeOldPwd = oldPwd || '';

    try {
      if (selectedFile) {
        const formData = new FormData();
        formData.append('file', selectedFile);
        const res = await fetch('http://localhost:8080/api/usuarios/perfil/foto', {
          method: 'PUT',
          headers: { 'Authorization': `Bearer ${token}` },
          body: formData
        });
        if (!res.ok) throw new Error('Falló subida de foto');
        messages.push('Foto actualizada');
      }

    
      if (safeNombre.trim() !== profile.nombreUsuario && safeNombre.trim() !== '') {
        
        const res = await fetch('http://localhost:8080/api/usuarios/nombre-usuario', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
          body: JSON.stringify({ nombreUsuario: safeNombre.trim() })
        });
        if (!res.ok) throw new Error('El nombre de usuario ya existe o error');
        messages.push('Nombre actualizado');
      }

      
      if (safeEmail.trim() !== profile.email && safeEmail.trim() !== '') {
        
        const res = await fetch('http://localhost:8080/api/usuarios/email', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
          body: JSON.stringify({ email: safeEmail.trim() })
        });
        if (!res.ok) throw new Error('Error al actualizar correo');
        messages.push('Correo actualizado');
      }

      if (safePwd !== '') {
        if (safePwd !== safeConfirmPwd) throw new Error('Las nuevas contraseñas no coinciden');
        if (safeOldPwd === '') throw new Error('Debes poner tu contraseña actual para cambiarla');
        
        const res = await fetch('http://localhost:8080/api/usuarios/password', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
          body: JSON.stringify({ oldPassword: safeOldPwd, newPassword: safePwd })
        });
        
        if (!res.ok) {
           const txt = await res.text();
           throw new Error(txt || 'Error al cambiar contraseña');
        }
        messages.push('Contraseña actualizada');
      }

      if (messages.length > 0) {
        alert('Cambios guardados:\n- ' + messages.join('\n- '));
        if (typeof fetchProfileData === 'function') {
            fetchProfileData(token); 
        }
      } else {
        if (safeNombre.trim() === '' && safeEmail.trim() === '' && safePwd === '' && !selectedFile) {
             setError('Los campos no pueden estar vacíos para ser actualizados.');
        } else {
             setError('No se detectaron cambios.');
        }
      }

    } catch (err) {
      console.error(err);
      setError(err.message);
    }
  };

  const handleLoginSubmit = (event) => {
    event.preventDefault();
    setError('');
    const loginUrl = 'http://localhost:8080/api/auth/login';
    const loginData = { email: email.trim(), pwd: pwd };

    fetch(loginUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginData)
    })
    .then(response => {
      if (!response.ok) {
        return response.json().then(errorData => {
          throw new Error(errorData.message || 'Email o contraseña incorrectos');
        }).catch(() => {
          throw new Error('Email o contraseña incorrectos');
        });
      }
      return response.text();
    })
    .then(token => {
      console.log('Login exitoso:', token);
      localStorage.setItem('token', token);
      fetchProfileData(token);
      setView('home');
    })
    .catch(error => {
      console.error('Error en el login:', error.message);
      setError(error.message);
    });
  };

  const handleRegisterSubmit = (event) => {
    event.preventDefault();
    setError('');
    if (pwd !== confirmPwd) {
      setError('Las contraseñas no coinciden');
      return;
    }
    const registerUrl = 'http://localhost:8080/api/auth/register';
    const registerData = {
      nombreUsuario: nombreUsuario.trim(),
      email: email.trim(),
      pwd: pwd
    };

    fetch(registerUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(registerData)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Error al registrar. El email o usuario ya existe.');
      }
      return response.json();
    })
    .then(data => {
      console.log('Registro exitoso:', data);
      alert('¡Te has registrado con éxito! Revisa tu email para verificar tu cuenta.');
      setView('iniciar sesión');
      setNombreUsuario(''); setEmail(''); setPwd(''); setConfirmPwd('');
    })
    .catch(error => {
      console.error('Error en el registro:', error);
      setError(error.message);
    });
  };

  const handleForgotPasswordSubmit = (event) => {
    event.preventDefault();
    setError('');
    const forgotUrl = 'http://localhost:8080/api/auth/forgot-password';
    fetch(forgotUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.trim() })
    })
    .then(response => {
      if (!response.ok) throw new Error('Error al procesar la solicitud.');
      return response.text(); 
    })
    .then(data => {
      alert('Si el correo existe en nuestro sistema, recibirás un email con las instrucciones.');
      setView('iniciar sesión');
      setEmail('');
    })
    .catch(error => {
      alert('Si el correo existe en nuestro sistema, recibirás un email con las instrucciones.');
      setView('iniciar sesión');
    });
  };

  const handleResetPasswordSubmit = (event) => {
    event.preventDefault();
    setError('');
    if (pwd !== confirmPwd) {
      setError('Las contraseñas no coinciden');
      return;
    }
    const resetUrl = `http://localhost:8080/api/auth/reset-password?token=${token}`;
    const resetData = { pwd: pwd };

    fetch(resetUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(resetData)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('El enlace no es válido o ha expirado. Por favor, solicita uno nuevo.');
      }
      return response.text();
    })
    .then(data => {
      alert('¡Contraseña cambiada con éxito! Ahora puedes iniciar sesión.');
      setPwd(''); setConfirmPwd(''); setToken('');
      setView('iniciar sesión');
      window.history.pushState({}, '', '/'); 
    })
    .catch(error => {
      console.error('Error al restablecer:', error);
      setError(error.message);
    });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setProfile(null);
    setError('');
    setView('welcome');
  };

  const handleDeleteAccount = () => {
    if (!window.confirm('¿Estás seguro de que quieres darte de baja?')) return;
    setError('');
    const token = localStorage.getItem('token');
    const deleteUrl = 'http://localhost:8080/api/usuarios/me';

    fetch(deleteUrl, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudo procesar la baja de la cuenta.');
      return response.text();
    })
    .then(message => {
      alert('Tu cuenta ha sido dada de baja. Serás desconectado.');
      handleLogout(); 
    })
    .catch(error => {
      console.error('Error al dar de baja:', error);
      alert(error.message);
    });
  };

  const handleNameSubmit = (event) => {
    event.preventDefault();
    setError('');
    if (nombreUsuario.trim() === '') {
      setError('El nombre de usuario no puede estar vacío.');
      return;
    }
    const token = localStorage.getItem('token');
    const url = 'http://localhost:8080/api/usuarios/nombre-usuario';
    const data = { nombreUsuario: nombreUsuario.trim() }; 

    fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(data)
    })
    .then(response => {
      if (!response.ok) return response.json().then(err => { throw new Error(err.message || 'Error al cambiar el nombre.'); });
      return response.json();
    })
    .then(updatedProfile => {
      setProfile(updatedProfile);
      alert('¡Nombre actualizado con éxito!');
      setView('home');
    })
    .catch(error => {
      console.error('Error al actualizar nombre:', error);
      setError(error.message);
    });
  };

  const handlePasswordChangeSubmit = (event) => {
    event.preventDefault();
    setError('');
    if (pwd !== confirmPwd) {
      setError('Las nuevas contraseñas no coinciden.');
      return;
    }
    if (oldPwd === pwd) {
      setError('La nueva contraseña no puede ser igual a la antigua.');
      return;
    }
    const token = localStorage.getItem('token');
    const url = 'http://localhost:8080/api/usuarios/password';
    const data = { oldPassword: oldPwd, newPassword: pwd };

    fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(data)
    })
    .then(response => {
      if (!response.ok) return response.text().then(text => { throw new Error(text || 'Error al cambiar la contraseña.'); });
      return response.text();
    })
    .then(message => {
      alert(message);
      setView('home');
    })
    .catch(error => {
      console.error('Error al actualizar contraseña:', error);
      setError(error.message);
    });
  };

  const handleEmailSubmit = (event) => {
    event.preventDefault();
    setError('');
    if (email.trim() !== confirmEmail.trim()) {
      setError('Los correos electrónicos no coinciden.');
      return;
    }
    if (email.trim() === profile.email) {
      setError('El nuevo correo es igual al actual.');
      return;
    }
    const token = localStorage.getItem('token');
    const url = 'http://localhost:8080/api/usuarios/email';
    const data = { email: email.trim() }; 

    fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(data)
    })
    .then(response => {
      if (!response.ok) return response.json().then(err => { throw new Error(err.message || 'Error al cambiar el correo.'); });
      return response.json();
    })
    .then(updatedProfile => {
      setProfile(updatedProfile);
      alert('¡Correo actualizado con éxito!');
      setView('home');
    })
    .catch(error => {
      console.error('Error al actualizar correo:', error);
      setError(error.message);
    });
  };

  const handleProfilePictureUpload = (file) => {
    if (!file) return;
    setError('');
    const token = localStorage.getItem('token');
    const uploadUrl = 'http://localhost:8080/api/usuarios/perfil/foto';
    const formData = new FormData();
    formData.append('file', file);

    fetch(uploadUrl, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${token}` },
      body: formData
    })
    .then(response => {
      if (!response.ok) throw new Error('Error al subir la imagen.');
      return response.json();
    })
    .then(updatedProfile => {
      setProfile(updatedProfile);
      alert('¡Foto de perfil actualizada!');
      setView('home');
    })
    .catch(error => {
      console.error('Error al subir foto:', error);
      setError(error.message);
    });
  };
    
  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      const objectUrl = URL.createObjectURL(file);
      setPreviewUrl(objectUrl);
    }
  };

  const triggerFileSelect = () => {
    fileInputRef.current.click();
  };

  const handleFollowRequest = (usernameToFollow) => {
    setError('');
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/relaciones/seguir/${usernameToFollow}`;
    fetch(url, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudo enviar la solicitud.');
      return response.text();
    })
    .then(message => {
      setViewedProfile(prevProfile => ({ ...prevProfile, relationshipStatus: 'PENDIENTE' }));
      alert('¡Solicitud enviada!');
    })
    .catch(error => alert(error.message));
  };

  const handleAcceptRequest = (usernameEmisor) => {
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/relaciones/solicitudes/aceptar/${usernameEmisor}`;
    fetch(url, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudo aceptar la solicitud.');
      return response.text();
    })
    .then(message => {
      alert(`¡Has aceptado a ${usernameEmisor}!`);
      setPendingRequests(currentRequests =>
        currentRequests.filter(user => user.nombreUsuario !== usernameEmisor)
      );
      fetchProfileData(token);
    })
    .catch(error => alert(error.message));
  };

  const handleRejectRequest = (usernameEmisor) => {
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/relaciones/solicitudes/rechazar/${usernameEmisor}`;
    fetch(url, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudo rechazar la solicitud.');
      return response.text();
    })
    .then(message => {
      alert(`Has rechazado a ${usernameEmisor}.`);
      setPendingRequests(currentRequests =>
        currentRequests.filter(user => user.nombreUsuario !== usernameEmisor)
      );
    })
    .catch(error => alert(error.message));
  };
  
  const handleRemoveFollower = (usernameToRemove) => {
    if (!window.confirm(`¿Estás seguro de que quieres eliminar a ${usernameToRemove} de tus seguidores?`)) return;
    setError('');
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/relaciones/seguidores/${usernameToRemove}`;
    fetch(url, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudo eliminar al seguidor.');
      return response.text();
    })
    .then(message => {
      alert(`Has eliminado a ${usernameToRemove} de tus seguidores.`);
      setUserList(currentList => 
        currentList.filter(user => user.nombreUsuario !== usernameToRemove)
      );
      fetchProfileData(token); 
    })
    .catch(error => setError(error.message));
  };

  const handleUnfollow = (usernameToUnfollow) => {
    if (!window.confirm(`¿Estás seguro de que quieres dejar de seguir a ${usernameToUnfollow}?`)) return;
    setError('');
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/relaciones/dejar-de-seguir/${usernameToUnfollow}`;
    fetch(url, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudo dejar de seguir.');
      return response.text();
    })
    .then(message => {
      alert(message);
      setViewedProfile(prevProfile => ({ ...prevProfile, relationshipStatus: null }));
    })
    .catch(error => alert(error.message));
  };

  const handleShowFollowers = () => {
    console.log('Mostrando seguidores...');
    setListTitle('Seguidores');
    setError('');
    const token = localStorage.getItem('token');
    fetch('http://localhost:8080/api/relaciones/seguidores', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => {
      if (!res.ok) throw new Error('No se pudieron cargar los seguidores.');
      return res.json();
    })
    .then(data => {
      setUserList(data);
      setView('userList');
    })
    .catch(err => setError(err.message));
  };

  const handleShowFollowing = () => {
    console.log('Mostrando seguidos...');
    setListTitle('Seguidos');
    setError('');
    const token = localStorage.getItem('token');
    fetch('http://localhost:8080/api/relaciones/siguiendo', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => {
      if (!res.ok) throw new Error('No se pudo cargar la lista de seguidos.');
      return res.json();
    })
    .then(data => {
      setUserList(data);
      setView('userList');
    })
    .catch(err => setError(err.message));
  };

  const handleShowOtherUserFollowers = (username) => {
    console.log(`Mostrando seguidores de: ${username}`);
    setListTitle(`Seguidores de ${username}`);
    setError('');
    const token = localStorage.getItem('token');
    fetch(`http://localhost:8080/api/relaciones/${username}/seguidores`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => {
      if (!res.ok) throw new Error('No se pudieron cargar los seguidores de este usuario.');
      return res.json();
    })
    .then(data => {
      setUserList(data);
      setView('userList');
    })
    .catch(err => {
      console.error(err);
      setError(err.message);
      setView('userProfile'); 
    });
  };

  const handleShowOtherUserFollowing = (username) => {
    console.log(`Mostrando a quién sigue: ${username}`);
    setListTitle(`Seguidos por ${username}`);
    setError('');
    const token = localStorage.getItem('token');
    fetch(`http://localhost:8080/api/relaciones/${username}/siguiendo`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => {
      if (!res.ok) throw new Error('No se pudo cargar la lista de seguidos de este usuario.');
      return res.json();
    })
    .then(data => {
      setUserList(data);
      setView('userList');
    })
    .catch(err => {
      console.error(err);
      setError(err.message);
      setView('userProfile');
    });
  };

  const handleFilterSubmit = (event, page = 0) => {
    if (event) event.preventDefault();
    console.log(`Buscando restaurantes con filtros: ciudad=${filterCiudad}, nombre=${filterNombre}, pagina=${page}`);
    setError('');
    setSearchPage(page);

    const token = localStorage.getItem('token');
    const params = new URLSearchParams();
    if (filterNombre) params.append('nombre', filterNombre);
    if (filterCiudad) params.append('ciudad', filterCiudad);
    if (filterTipoCocina) params.append('tipoCocina', filterTipoCocina);
    if (filterDireccion) params.append('direccion', filterDireccion);
    params.append('page', page);
    params.append('size', 10);

    
    const url = `http://localhost:8080/api/restaurantes/externos/buscar?${params.toString()}`;

    fetch(url, {
    })
    .then(response => {
      if (!response.ok) throw new Error('Error al buscar restaurantes');
      return response.json();
    })
    .then(data => {
      console.log("Restaurantes encontrados:", data);
      setRestaurantResults(data);
      handleShowRestaurantResults();
    })
    .catch(err => {
      console.error("Error buscando restaurantes:", err);
      setError("No se pudieron buscar restaurantes.");
    });
  };

 
  const handleSearchPagination = (newPage) => {
    handleFilterSubmit(null, newPage);
  };

  const handleToggleFavorite = (restauranteObject) => {
    const token = localStorage.getItem('token');
    
    const isExternalObject = restauranteObject.tags !== undefined;
    const externalId = (isExternalObject ? restauranteObject.id : restauranteObject.osmId).toString();
    
    if (favoritedRestaurantsMap.has(externalId)) {
      
      const localIdToDelete = favoritedRestaurantsMap.get(externalId);
      console.log(`Eliminando favorito (ID local: ${localIdToDelete})...`);

      fetch(`http://localhost:8080/api/favoritos/${localIdToDelete}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      })
      .then(response => {
        if (!response.ok) throw new Error('Error al eliminar de favoritos.');
        return response.text();
      })
      .then(() => {
        alert('Restaurante eliminado de favoritos.');
        
        setFavoritedRestaurantsMap(prevMap => {
          const newMap = new Map(prevMap);
          newMap.delete(externalId);
          return newMap;
        });
        
        if (favoritesListResults) {
          setFavoritesListResults(prevResults => {
            const newContent = prevResults.content.filter(item => item.osmId.toString() !== externalId);
            return { ...prevResults, content: newContent, totalElements: newContent.length }; 
          });
        }
      })
      .catch(error => {
        console.error('Error al eliminar favorito:', error);
        setError('No se pudo eliminar de favoritos.');
      });

    } else {
      
      
      if (!isExternalObject) {
         console.error("Error: Se intentó añadir un favorito desde un objeto local.", restauranteObject);
         return; 
      }

      console.log("Importando y añadiendo a favoritos...");
      const importDTO = {
        osmId: restauranteObject.id,
        nombre: restauranteObject.tags?.name || 'Nombre no disponible',
        ciudad: restauranteObject.tags?.['addr:city'] || filterCiudad,
        direccion: restauranteObject.tags?.['addr:street'] || '',
        tipoCocina: restauranteObject.tags?.cuisine || 'No especificada',
        lat: restauranteObject.lat,
        lon: restauranteObject.lon
      };

      fetch('http://localhost:8080/api/restaurantes/importar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(importDTO)
      })
      .then(response => {
        if (!response.ok) throw new Error('Error al importar el restaurante.');
        return response.json(); 
      })
      .then(restauranteLocal => {
        return fetch(`http://localhost:8080/api/favoritos/${restauranteLocal.id}`, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` }
        }).then(res => {
          if (!res.ok) throw new Error('Error al añadir a favoritos.');
          return { response: res, restauranteLocal };
        });
      })
      .then(({ response, restauranteLocal }) => {
        alert(`¡'${importDTO.nombre}' añadido a favoritos!`);
        
        setFavoritedRestaurantsMap(prevMap =>
          new Map(prevMap).set(externalId, restauranteLocal.id)
        );
        setFavoritedRestaurantsMap(prevMap =>
          new Map(prevMap).set(externalId, restauranteLocal.id)
        );
      })
      .catch(error => {
        console.error('Error al añadir favorito:', error);
        setError('No se pudo añadir a favoritos.');
      });
    }
  };

  const handleShowFavorites = (page = 0) => {
    console.log("Cargando lista de favoritos, página:", page);
    setError('');
    setFavoritesPage(page);
    const token = localStorage.getItem('token');
    
    const url = `http://localhost:8080/api/favoritos?page=${page}&size=5`;

    fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudieron cargar los favoritos.');
      return response.json();
    })
    .then(data => {
      
      setFavoritesListResults(data);
      setView('favoritesList');
    })
    .catch(err => {
      console.error("Error cargando favoritos:", err);
      setError("No se pudieron cargar tus favoritos.");
      handleBackToHome();
    });
  };


  const handleFavoritesPagination = (newPage) => {
    handleShowFavorites(newPage);
  };


  const handleShowCreateReview = () => {
    setReviewRating(0);
    setReviewComment('');
    setReviewPhotos([]);
    setView('createReview');
  };

  
 const handleSubmitReview = async (event) => {
    event.preventDefault();
    if (reviewRating === 0) {
      setError('Debes seleccionar una puntuación.');
      return;
    }
    setError('');

    try {
      // 1. Prepara el DTO
      const valoracionInputDTO = {
        nombreRestaurante: selectedRestaurant.tags.name || 'Nombre no disponible',
        ciudad: selectedRestaurant.tags['addr:city'] || filterCiudad,
        puntuacion: reviewRating,
        comentario: reviewComment
      };

      // 2. Crea el FormData
      const formData = new FormData();
      formData.append('valoracion', JSON.stringify(valoracionInputDTO));

      // 3. Anade los archivos (fotos)
      if (reviewPhotos && reviewPhotos.length > 0) {
        for (let i = 0; i < reviewPhotos.length; i++) {
          formData.append('files', reviewPhotos[i]);
        }
      }

      const token = localStorage.getItem('token');
      const url = `http://localhost:8080/api/valoraciones/usuario/${profile.nombreUsuario}`;

      // 4. Envia la peticion
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText);
      }

      alert('¡Reseña guardada con éxito!');
      handleBackToPreviousView();

    } catch (err) {
      console.error(err);
    
      try {
        const errorJson = JSON.parse(err.message);
        if (errorJson && errorJson.message) {
          setError(errorJson.message);
        } else {

          setError('No se pudo guardar la reseña. Error desconocido.');
        }
      } catch (parseError) {
        setError(err.message);
      }
    }
  };

  const handleDeleteReview = async () => {
    // 1. Confirma con el usuario
    if (!window.confirm('¿Estás seguro de que quieres eliminar esta reseña?')) {
      return;
    }

    const restauranteId = selectedReview.restaurante.id;
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/valoraciones/${restauranteId}`;

    try {
      // 2. Llama al nuevo endpoint DELETE
      const response = await fetch(url, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!response.ok) {
        throw new Error('No se pudo eliminar la reseña.');
      }

      alert('Reseña eliminada con éxito.');

      // 3. Actualiza el estado local
      setProfile(prevProfile => ({
        ...prevProfile,
        valoraciones: prevProfile.valoraciones.filter(v => v.restaurante.id !== restauranteId)
      }));

      if (feedResults) {
        setFeedResults(prevFeed => ({
          ...prevFeed,
          content: prevFeed.content.filter(v => v.restaurante.id !== restauranteId)
        }));
      }
      // 4. Vuelve a la pantalla anterior
      handleBackToPreviousView();

    } catch (err) {
      console.error(err);
      setError(err.message);
    }
  };

  const handlePhotoSelect = (event) => {
    if (event.target.files) {
      const newFilesArray = Array.from(event.target.files);
      setReviewPhotos(prevPhotos => [...prevPhotos, ...newFilesArray]);
    }
  };

  const handleShowReviewDetail = (valoracion) => {
    setSelectedReview(valoracion);
    setPreviousView(view);
    setView('reviewDetail');
  };

  const handleShowFeed = (page = 0) => {
    console.log("Cargando feed, página:", page);
    setError('');
    setFeedPage(page);
    const token = localStorage.getItem('token');
    
    const url = `http://localhost:8080/api/valoraciones/feed?page=${page}&size=10`;

    fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
      if (!response.ok) throw new Error('No se pudo cargar el feed.');
      return response.json();
    })
    .then(data => {
      setFeedResults(data);
      setView('feed');
    })
    .catch(err => {
      console.error("Error cargando el feed:", err);
      setError("No se pudo cargar tu feed.");
    });
  };

  const handleFeedPagination = (newPage) => {
    handleShowFeed(newPage);
  };


  // --- 7. LOGICA DE RENDERIZADO ---
  const renderContent = () => {
    
    if (view === 'welcome') {
      return (
        <div className="welcome-content">
          <h1>Bienvenido a GastroLog!!</h1>
          <button className="auth-button" onClick={handleContinue}>Continuar</button>
        </div>
      );
    }
    if (view === 'opciones') {
      return (
        <>
          <h2>Elige una opción</h2>
          <div className="auth-options">
            <button className="auth-button" onClick={handleShowLogin}>Iniciar Sesión</button>
            <button className="auth-button" onClick={handleShowRegister}>Registrarse</button>
          </div>
        </>
      );
    }
    if (view === 'iniciar sesión') {
      return (
        <>
          <h2>Iniciar Sesión</h2>
          <form className="auth-form" onSubmit={handleLoginSubmit}>
            <input type="email" placeholder="Correo electrónico" className="auth-input" value={email} onChange={(e) => setEmail(e.target.value)} required />
            <input type="password" placeholder="Contraseña" className="auth-input" value={pwd} onChange={(e) => setPwd(e.target.value)} required />
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="auth-button submit">Entrar</button>
            <button type="button" className="forgot-password-button" onClick={handleShowForgotPassword}>¿Has olvidado tu contraseña?</button>
          </form>
          <button className="back-button" onClick={handleBack}>← Volver</button>
        </>
      );
    }
    if (view === 'registro') {
      return (
        <>
          <h2>Registrarse</h2>
          <form className="auth-form" onSubmit={handleRegisterSubmit}>
            <input type="text" placeholder="Nombre de usuario" className="auth-input" value={nombreUsuario} onChange={(e) => setNombreUsuario(e.target.value)} required />
            <input type="email" placeholder="Correo electrónico" className="auth-input" value={email} onChange={(e) => setEmail(e.target.value)} required />
            <input type="password" placeholder="Contraseña" className="auth-input" value={pwd} onChange={(e) => setPwd(e.target.value)} required />
            <input type="password" placeholder="Confirmar Contraseña" className="auth-input" value={confirmPwd} onChange={(e) => setConfirmPwd(e.target.value)} required />
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="auth-button submit">Crear cuenta</button>
          </form>
          <button className="back-button" onClick={handleBack}>← Volver</button>
        </>
      );
    }
    if (view === 'recuperar') {
      return (
        <>
          <h2>Recuperar Contraseña</h2>
          <p className="info-text">Introduce tu correo electrónico. Te enviaremos un enlace para restablecer tu contraseña.</p>
          <form className="auth-form" onSubmit={handleForgotPasswordSubmit}>
            <input type="email" placeholder="Correo electrónico" className="auth-input" value={email} onChange={(e) => setEmail(e.target.value)} required />
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="auth-button submit">Enviar enlace</button>
          </form>
          <button className="back-button" onClick={handleShowLogin}>← Volver a Iniciar Sesión</button>
        </>
      );
    }
    if (view === 'restablecer') {
      return (
        <>
          <h2>Restablecer Contraseña</h2>
          <p className="info-text">Token detectado. Por favor, introduce tu nueva contraseña.</p>
          <form className="auth-form" onSubmit={handleResetPasswordSubmit}>
            <input type="password" placeholder="Nueva Contraseña" className="auth-input" value={pwd} onChange={(e) => setPwd(e.target.value)} required />
            <input type="password" placeholder="Confirmar Nueva Contraseña" className="auth-input" value={confirmPwd} onChange={(e) => setConfirmPwd(e.target.value)} required />
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="auth-button submit">Cambiar Contraseña</button>
          </form>
          <button className="back-button" onClick={() => setView('iniciar sesión')}>← Cancelar</button>
        </>
      );
    }
    if (view === 'home') {
      if (error) {
        return (
          <div className="error-message">
            <p>{error}</p>
            <button className="auth-button" onClick={handleShowLogin}>Volver al Login</button>
          </div>
        );
      }
      if (!profile) {
        return <div className="loading">Cargando perfil...</div>;
      }
      return (
        <div className="profile-container">
          <header className="profile-header">
            <button className="menu-button" onClick={handleShowMenu}>
              <FaBars />
            </button>
            <span className="profile-username">{profile.nombreUsuario}</span>
          </header>
          <section className="profile-info">
            <img 
              src={profile.fotoPerfilUrl || 'https://via.placeholder.com/150'} 
              alt="Foto de perfil" 
              className="profile-picture"
              onClick={() => handleShowImageModal(profile.fotoPerfilUrl)} 
            />
          </section>
          <section className="profile-stats">
            <div onClick={handleShowFollowing}>
              <span>{profile.seguidosCount}</span>
              Seguidos
            </div>
            <div onClick={handleShowFollowers}>
              <span>{profile.seguidoresCount}</span>
              Seguidores
            </div>
          </section>
          <section className="profile-buttons">
            <button className="profile-button favorites" onClick={() => handleShowFavorites(0)}>
              <FaHeart /> Favoritos
            </button>
            <button className="profile-button requests" onClick={handleShowRequests}>
              <FaUserFriends /> Solicitudes ({profile.solicitudesPendientesCount})
            </button>
          </section>
          <section className="reviews-section">
            <h3 style={{ marginLeft: '10px' }}>Mis Reseñas</h3>
            
            {(!profile.valoraciones || profile.valoraciones.length === 0) ? (
              
              <div className="empty-state-card-profile">
                <div className="empty-icon-circle">
                   <FaUtensils />
                </div>
                <h4>Tu diario gastronómico está vacío</h4>
                <p>
                  Aún no has valorado ningún restaurante. ¡Busca tu lugar favorito y comparte tu experiencia con la comunidad!
                </p>
                <button className="action-button-primary" style={{marginTop: '20px'}} onClick={handleShowRestauranteSearch}>
                  <FaSearch style={{marginRight: '8px'}}/> Buscar Restaurante
                </button>
              </div>

            ) : (
              <div className="reviews-list">
                {profile.valoraciones.map(valoracion => {
                  
                  if (!valoracion.restaurante) return null;
                  
                  return (
                    <div 
                      key={valoracion.restaurante.id} 
                      className="review-card"
                      onClick={() => handleShowReviewDetail(valoracion)}
                    >
                      
                      {valoracion.fotos && valoracion.fotos.length > 0 ? (
                        <div className="review-card-image-container">
                           <img 
                              src={valoracion.fotos[0].url} 
                              alt={valoracion.restaurante.nombre} 
                              className="review-photo" 
                            />
                           <div className="review-image-overlay">
                              <FaImage /> {valoracion.fotos.length}
                           </div>
                        </div>
                      ) : (
                        <div className="review-card-placeholder">
                           <FaUtensils />
                        </div>
                      )}

                      <div className="review-card-content">
                        
                        <div className="review-card-header">
                          <span className="review-restaurant-title">{valoracion.restaurante.nombre}</span>
                          <div className="stars">
                            {[...Array(5)].map((_, i) => (
                              <FaStar 
                                key={i} 
                                color={i < valoracion.puntuacion ? '#f5c518' : '#e4e5e9'} 
                                size={14}
                              />
                            ))}
                          </div>
                        </div>
                        
                        <p className="review-body-text">
                          {valoracion.comentario || <span style={{color: '#999', fontStyle:'italic'}}>Sin comentario...</span>}
                        </p>

                        <div className="review-footer-hint">
                           Ver detalles →
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        </div>
      );
    }
    if (view === 'menu') {
      return (
        <div className="menu-container">
          
          <div className="menu-card">
            
            <div className="menu-profile-summary">
              <img 
                src={profile?.fotoPerfilUrl || 'https://via.placeholder.com/150'} 
                alt="Perfil" 
                className="menu-avatar"
              />
              <h3>{profile?.nombreUsuario}</h3>
              <p className="menu-email">{profile?.email}</p>
            </div>

            <div className="menu-divider"></div>

            <button className="menu-item-button" onClick={handleShowEditProfile}>
              <div className="menu-btn-content">
                <FaUserEdit className="menu-icon-blue"/> 
                <span>Editar Perfil</span>
              </div>
              <span className="arrow-icon">›</span>
            </button>

            <button className="menu-item-button logout" onClick={handleLogout}>
              <div className="menu-btn-content">
                <FaSignOutAlt /> 
                <span>Cerrar Sesión</span>
              </div>
            </button>

            <button className="menu-item-button danger" onClick={handleDeleteAccount}>
              <div className="menu-btn-content">
                <FaUserSlash /> 
                <span>Darse de baja</span>
              </div>
            </button>

          </div>

          <button className="back-button-simple" onClick={handleBackToHome}>
             Volver al Perfil
          </button>
        </div>
      );
    }
   if (view === 'editarPerfil') {
      return (
        <div className="menu-container">
          <header className="profile-header-simple">
             <button className="back-button" onClick={handleShowMenu}>
               <FaArrowLeft />
            </button>
            <h2 style={{margin: 0}}>Editar Perfil</h2>
          </header>

          <form className="auth-form" onSubmit={handleUnifiedSave} style={{marginTop: '20px'}}>
            
            <div className="edit-profile-header" onClick={triggerFileSelect}>
               <img 
                 src={previewUrl || profile.fotoPerfilUrl || 'https://via.placeholder.com/150'} 
                 alt="Avatar" 
                 className="edit-profile-img"
               />
               <div className="edit-icon-overlay">Cambiar Foto</div>
               <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileSelect}
                style={{ display: 'none' }}
                accept="image/png, image/jpeg"
              />
            </div>

            <label className="form-section-title">Información Pública</label>
            <input
              type="text"
              placeholder="Nombre de usuario"
              className="auth-input"
              value={nombreUsuario}
              onChange={(e) => setNombreUsuario(e.target.value)}
            />
            
            <label className="form-section-title">Información Privada</label>
            <input
              type="email"
              placeholder="Correo electrónico"
              className="auth-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />

            <div className="form-section-divider"></div>
            <label className="form-section-title">Cambiar Contraseña (Opcional)</label>
            <p className="info-text" style={{fontSize: '0.8em', marginBottom: '10px'}}>
              Rellena esto solo si quieres cambiar tu clave.
            </p>

            <input
              type="password"
              placeholder="Contraseña Actual (Requerida si cambias la nueva)"
              className="auth-input"
              value={oldPwd}
              onChange={(e) => setOldPwd(e.target.value)}
            />
            <input
              type="password"
              placeholder="Nueva Contraseña"
              className="auth-input"
              value={pwd}
              onChange={(e) => setPwd(e.target.value)}
            />
            <input
              type="password"
              placeholder="Confirmar Nueva Contraseña"
              className="auth-input"
              value={confirmPwd}
              onChange={(e) => setConfirmPwd(e.target.value)}
            />

            {error && <p className="error-message">{error}</p>}
            
            <button type="submit" className="auth-button submit">
              Guardar Cambios
            </button>
          </form>
        </div>
      );
    }
    if (view === 'editNombre') {
      return (
        <div className="menu-container">
          <h2>Editar Nombre</h2>
          <p className="info-text">Introduce tu nuevo nombre de usuario.</p>
          <form className="auth-form" onSubmit={handleNameSubmit}>
            <input
              type="text"
              placeholder="Nuevo nombre de usuario"
              className="auth-input"
              value={nombreUsuario}
              onChange={(e) => setNombreUsuario(e.target.value)}
              required
            />
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="auth-button submit">Guardar Cambios</button>
          </form>
          <button className="back-button" onClick={handleShowEditProfile}>
            ← Volver a Editar Perfil
          </button>
        </div>
      );
    }
    if (view === 'editPassword') {
      return (
        <div className="menu-container">
          <h2>Editar Contraseña</h2>
          <form className="auth-form" onSubmit={handlePasswordChangeSubmit}>
            <input
              type="password"
              placeholder="Contraseña Antigua"
              className="auth-input"
              value={oldPwd}
              onChange={(e) => setOldPwd(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Nueva Contraseña"
              className="auth-input"
              value={pwd}
              onChange={(e) => setPwd(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Confirmar Nueva Contraseña"
              className="auth-input"
              value={confirmPwd}
              onChange={(e) => setConfirmPwd(e.target.value)}
              required
            />
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="auth-button submit">Guardar Contraseña</button>
          </form>
          <button className="back-button" onClick={handleShowEditProfile}>
            ← Volver a Editar Perfil
          </button>
        </div>
      );
    }
    if (view === 'editEmail') {
      return (
        <div className="menu-container">
          <h2>Editar Correo</h2>
          <p className="info-text">Introduce tu nuevo correo electrónico.</p>
          <form className="auth-form" onSubmit={handleEmailSubmit}>
            <input
              type="email"
              placeholder="Nuevo correo"
              className="auth-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <input
              type="email"
              placeholder="Confirmar correo"
              className="auth-input"
              value={confirmEmail}
              onChange={(e) => setConfirmEmail(e.target.value)}
              required
            />
            {error && <p className="error-message">{error}</p>}
            <button type="submit" className="auth-button submit">Guardar Cambios</button>
          </form>
          <button className="back-button" onClick={handleShowEditProfile}>
            ← Volver a Editar Perfil
          </button>
        </div>
      );
    }
   if (view === 'search') {
      const isSearchActive = searchQuery.trim() !== '';

      return (
        <div className="search-container">
          
          <button 
            className="back-button" 
            onClick={handleBackToHome}
            style={{ 
              position: 'fixed', 
              top: '20px', 
              left: '20px', 
              zIndex: 200,
              background: 'white',
              padding: '10px',
              borderRadius: '50%',
              boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
              width: '40px',
              height: '40px',
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center'
            }}
          >
            <FaArrowLeft />
          </button>

          <div className={`search-hero-container ${isSearchActive ? 'active-mode' : ''}`}>
            
            <div className={`hero-content-wrapper ${isSearchActive ? 'hidden' : ''}`}>
              <div className="hero-icon-circle">
                <FaUserFriends />
              </div>
              <h2 className="hero-title">Encuentra a tus amigos</h2>
              <p className="hero-subtitle">
                Conecta con otros foodies y descubre sus restaurantes favoritos.
              </p>
            </div>

            <div className="hero-search-bar">
              <FaSearch className="hero-search-icon" />
              <input
                type="text"
                placeholder="Escribe un nombre de usuario..."
                className="hero-search-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                autoFocus
              />
            </div>

            {isSearchActive && (
              <div className="hero-results-container">
                
                {searchResults.length === 0 && (
                  <div className="empty-state-container" style={{ minHeight: 'auto', padding: '40px 0' }}>
                     <p className="empty-state-text">No se encontraron usuarios.</p>
                  </div>
                )}

                {searchResults.map(user => (
                  <div 
                    key={user.id} 
                    className="result-item"
                    onClick={() => handleShowUserProfile(user.nombreUsuario)}
                    style={{ border: '1px solid #eee', marginBottom: '10px' }} 
                  >
                    {user.nombreUsuario}
                  </div>
                ))}
              </div>
            )}

          </div>
        </div>
      );
    }
    if (view === 'userProfile') {
      if (!viewedProfile) {
        return <div className="loading">Cargando perfil...</div>;
      }
      return (
        <div className="profile-container"> 
          
          <header className="profile-header-simple">
            <button className="back-button" onClick={handleShowSearch}>
              <FaArrowLeft /> Volver al buscador
            </button>
          </header>

          <div className="public-profile-card">
            
            <div className="public-profile-header">
              <img 
                src={viewedProfile.fotoPerfilUrl || 'https://via.placeholder.com/150'} 
                alt="Foto de perfil" 
                className="public-profile-pic"
                onClick={() => handleShowImageModal(viewedProfile.fotoPerfilUrl)}
              />
              
              <div className="public-profile-info">
                <div className="public-profile-name-row">
                  <h2>{viewedProfile.nombreUsuario}</h2>
                  
                  {viewedProfile.relationshipStatus === null && (
                    <button className="action-button-primary small" onClick={() => handleFollowRequest(viewedProfile.nombreUsuario)}>
                      Seguir
                    </button>
                  )}
                  {viewedProfile.relationshipStatus === 'PENDIENTE' && (
                    <button className="action-button-secondary" onClick={() => handleUnfollow(viewedProfile.nombreUsuario)}>
                      Solicitud Enviada
                    </button>
                  )}
                  {viewedProfile.relationshipStatus === 'ACEPTADO' && (
                    <button className="action-button-secondary" onClick={() => handleUnfollow(viewedProfile.nombreUsuario)}>
                      Siguiendo
                    </button>
                  )}
                </div>

                {viewedProfile.seguidoresCount === null ? (
                   <div className="privacy-notice">
                      <FaLock /> Este perfil es privado
                   </div>
                ) : (
                  <div className="public-profile-stats">
                    <div onClick={() => handleShowOtherUserFollowing(viewedProfile.nombreUsuario)}>
                      <span>{viewedProfile.seguidosCount}</span> Seguidos
                    </div>
                    <div onClick={() => handleShowOtherUserFollowers(viewedProfile.nombreUsuario)}>
                      <span>{viewedProfile.seguidoresCount}</span> Seguidores
                    </div>
                  </div>
                )}
              </div>
            </div>
            
            <div className="menu-divider"></div>

            <section className="public-profile-reviews">
               <h3 style={{marginBottom: '20px', color: '#666'}}>Reseñas de {viewedProfile.nombreUsuario}</h3>
               
               {(!viewedProfile.valoraciones || viewedProfile.valoraciones.length === 0) ? (
                 <div className="empty-state-container" style={{minHeight: '200px'}}>
                    <p className="info-text">Este usuario no ha publicado reseñas aún.</p>
                 </div>
               ) : (
                 <div className="detail-grid"> 
                    {viewedProfile.valoraciones.map(valoracion => {
                       if (!valoracion.restaurante) return null;
                       return (
                         <div 
                            key={valoracion.restaurante.id} 
                            className="public-review-card"
                            onClick={() => handleShowReviewDetail(valoracion)}
                         >
                            <div className="public-review-header">
                              <span className="restaurant-name">{valoracion.restaurante.nombre}</span>
                              <div className="stars-row">
                                {[...Array(5)].map((_, i) => (
                                  <FaStar key={i} color={i < valoracion.puntuacion ? '#f5c518' : '#e4e5e9'} size={14}/>
                                ))}
                              </div>
                            </div>
                            <p className="public-review-text">
                              {valoracion.comentario ? `"${valoracion.comentario}"` : <i>Sin comentario</i>}
                            </p>
                            {valoracion.fotos && valoracion.fotos.length > 0 && (
                               <div className="public-review-img-badge" style={{fontSize: '0.8em', color: '#1877f2', marginTop: '10px'}}>
                                 <FaImage /> Contiene fotos
                               </div>
                            )}
                         </div>
                       );
                    })}
                 </div>
               )}
            </section>

          </div>
        </div>
      );
    }
  if (view === 'requests') {
      return (
        <div className="requests-view-container">
          <header className="section-header-pro">
            <button className="back-btn-circle" onClick={handleBackToHome}>
              <FaArrowLeft />
            </button>
            <h2>Solicitudes</h2>
            <div style={{ width: '40px' }}></div> 
          </header>

          <div className="requests-content-wrapper">
            
            {pendingRequests.length === 0 ? (
              <div className="empty-state-container">
                <FaCheck className="empty-state-icon" />
                <div className="empty-state-title">Estás al día</div>
                <p className="empty-state-text">
                  No tienes solicitudes de seguimiento pendientes.
                </p>
              </div>
            ) : (
              <div className="requests-grid">
                {pendingRequests.map(user => (
                  <div key={user.id} className="request-card-pro">
                    
                    <div className="request-card-left">
                      <div className="request-avatar-circle">
                         <FaUser /> 
                      </div>
                      <div className="request-user-details">
                        <span 
                            className="request-username-pro"
                            onClick={() => handleShowUserProfile(user.nombreUsuario)} 
                        >
                            {user.nombreUsuario}
                        </span>
                        <span className="request-subtitle">Quiere seguirte</span>
                      </div>
                    </div>

                    <div className="request-card-actions">
                      <button 
                        className="req-action-btn accept" 
                        onClick={() => handleAcceptRequest(user.nombreUsuario)}
                      >
                        <FaCheck /> Confirmar
                      </button>
                      <button 
                        className="req-action-btn reject" 
                        onClick={() => handleRejectRequest(user.nombreUsuario)}
                      >
                        <FaTimes /> Eliminar
                      </button>
                    </div>

                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      );
    }
    if (view === 'userList') {
      return (
        <div className="search-container">
          <header className="search-header">
            <button className="back-button" onClick={handleBackToHome}>
              <FaArrowLeft />
            </button>
            <h2>{listTitle}</h2>
          </header>
          <div className="results-list">
            
            {(!userList || userList.length === 0) ? (
              <p className="info-text">No hay usuarios que mostrar.</p>
            ) : (
              userList.map(user => (
                <div key={user.id} className="result-item">
                  
                  <span 
                    className="result-item-name"
                    onClick={() => handleShowUserProfile(user.nombreUsuario)}
                  >
                    {user.nombreUsuario}
                  </span>

                  {listTitle === 'Seguidores' && (
                    <button 
                      className="remove-button"
                      onClick={() => handleRemoveFollower(user.nombreUsuario)}
                    >
                      <FaTimes /> Eliminar
                    </button>
                  )}

                </div>
              ))
            )}
          </div>
        </div>
      );
    }
    if (view === 'filterRestaurantes') {
      return (
        <div className="restaurant-search-hero">
          
          <button className="hero-back-btn" onClick={handleBackToHome}>
            <FaArrowLeft /> Volver al Inicio
          </button>

          <div className="search-overlay-card">
            <div className="search-card-header">
              <FaUtensils className="header-icon"/>
              <h2>Descubre tu próximo destino</h2>
              <p>Filtra por nombre, ciudad o tipo de cocina</p>
            </div>

            <form className="pro-search-form" onSubmit={handleFilterSubmit}>
              
              <div className="search-grid-inputs">
                <div className="input-group">
                  <label>Nombre</label>
                  <div className="input-wrapper">
                    <FaSearch className="input-icon"/>
                    <input
                      type="text"
                      placeholder="Ej: Casa Pepe..."
                      className="pro-input"
                      value={filterNombre}
                      onChange={(e) => setFilterNombre(e.target.value)}
                    />
                  </div>
                </div>

                <div className="input-group">
                  <label>Ciudad</label>
                  <div className="input-wrapper">
                    <FaMapMarkerAlt className="input-icon"/>
                    <input
                      type="text"
                      placeholder="Ej: Madrid..."
                      className="pro-input"
                      value={filterCiudad}
                      onChange={(e) => setFilterCiudad(e.target.value)}
                    />
                  </div>
                </div>

                <div className="input-group">
                  <label>Cocina</label>
                  <div className="input-wrapper">
                    <FaUtensils className="input-icon"/>
                    <select
                      className="pro-input"
                      value={filterTipoCocina}
                      onChange={(e) => setFilterTipoCocina(e.target.value)}
                    >
                      {commonCuisines.map(cuisine => (
                        <option key={cuisine.value} value={cuisine.value}>
                          {cuisine.label}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="input-group">
                  <label>Dirección</label>
                  <div className="input-wrapper">
                    <FaMapMarkerAlt className="input-icon"/>
                    <input
                      type="text"
                      placeholder="Calle, Avenida..."
                      className="pro-input"
                      value={filterDireccion}
                      onChange={(e) => setFilterDireccion(e.target.value)}
                    />
                  </div>
                </div>
              </div>

              {error && <p className="error-message">{error}</p>}
              
              <button type="submit" className="action-button-primary full-width">
                <FaSearch /> Buscar Restaurantes
              </button>
            </form>
          </div>
        </div>
      );
    }
    if (view === 'restaurantResults') {
      return (
        <div className="search-container">
          <header className="search-header">
            <button className="back-button" onClick={handleShowRestauranteSearch}>
              <FaArrowLeft /> Volver a Filtros
            </button>
          </header>
          
          <div className="results-list">
            {(!restaurantResults || restaurantResults.content.length === 0) ? (
              <div className="empty-state-container">
                <FaUtensils className="empty-state-icon" />
                <div className="empty-state-title">Sin resultados</div>
                <p className="empty-state-text">
                  No hay restaurantes que coincidan con tus filtros. Intenta ser menos específico.
                </p>
                <button className="auth-button" style={{marginTop: '20px'}} onClick={handleShowRestauranteSearch}>
                  Probar otra búsqueda
                </button>
              </div>
            ) : (
              restaurantResults.content.map(restaurante => {
                
                const isFavorited = favoritedRestaurantsMap.has(restaurante.id.toString());
                
                const nombre = restaurante.tags.name || 'Nombre no disponible';
                const ciudad = restaurante.tags['addr:city'] || filterCiudad || 'Ciudad desc.';
                const cocina = restaurante.tags.cuisine || 'Cocina general';
                const direccion = restaurante.tags['addr:street'] || '';
                
                const position = [restaurante.lat, restaurante.lon];

                return (
                  <div key={restaurante.id} className="restaurant-result-card-large">
                    
                    <div className="card-large-info">
                      <div className="card-header-row">
                        <h3 className="card-title" onClick={() => handleShowRestaurantDetail(restaurante)}>
                          {nombre}
                        </h3>
                        <button 
                          className={`card-fav-btn-simple ${isFavorited ? 'active' : ''}`}
                          onClick={() => handleToggleFavorite(restaurante)}
                        >
                          {isFavorited ? <FaBookmark /> : <FaRegBookmark />}
                        </button>
                      </div>

                      <div className="card-details-block" onClick={() => handleShowRestaurantDetail(restaurante)}>
                        <div className="detail-row">
                          <FaUtensils className="detail-icon"/> {cocina}
                        </div>
                        <div className="detail-row">
                          <FaMapMarkerAlt className="detail-icon"/> {ciudad}
                        </div>
                        {direccion && (
                          <div className="detail-row small-text">
                            {direccion}
                          </div>
                        )}
                        <span className="view-more-link">Ver ficha completa</span>
                      </div>
                    </div>

                    <div className="card-large-map">
                      <MapContainer 
                        key={`map-${restaurante.id}`}
                        center={position} 
                        zoom={15} 
                        scrollWheelZoom={false}
                        dragging={false}        
                        zoomControl={false}     
                        attributionControl={false}
                        style={{ height: '100%', width: '100%' }}
                      >
                        <TileLayer
                          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />
                        <Marker position={position} />
                      </MapContainer>
                    </div>

                  </div>
                );
              })
            )}
          </div>
          
          {restaurantResults && (
            <div className="pagination-controls">
              <button 
                onClick={() => handleSearchPagination(searchPage - 1)}
                disabled={restaurantResults.first}
                className="auth-button"
              >
                Anterior
              </button>
              <span>Página {searchPage + 1} de {restaurantResults.totalPages}</span>
              <button 
                onClick={() => handleSearchPagination(searchPage + 1)}
                disabled={restaurantResults.last}
                className="auth-button"
              >
                Siguiente
              </button>
            </div>
          )}
        </div>
      );
    }
   if (view === 'favoritesList') {
      return (
        <div className="search-container">
          <header className="favorites-header-pro">
            <button className="back-btn-circle" onClick={handleBackToHome}>
              <FaArrowLeft />
            </button>
            <h2>Mis Favoritos</h2>
            <div style={{ width: '40px' }}></div> 
          </header>

          <div className="results-list">
            
            {(!favoritesListResults || !favoritesListResults.content || favoritesListResults.content.length === 0) ? (
              <div className="empty-state-container">
                <FaHeart className="empty-state-icon" style={{ color: '#ff6b6b' }} />
                <div className="empty-state-title">Aún no tienes favoritos</div>
                <p className="empty-state-text">
                  Guarda los restaurantes que más te gusten pulsando en el icono de guardado para encontrarlos aquí rápidamente.
                </p>
                <button className="auth-button" style={{ marginTop: '20px' }} onClick={handleShowRestauranteSearch}>
                  Explorar Restaurantes
                </button>
              </div>
            ) : (
              
              favoritesListResults.content.map(restaurante => {
                
                const nombre = restaurante.nombre || 'Nombre no disponible';
                const ciudad = restaurante.ciudad || 'Ciudad no disponible';
                const cocina = restaurante.tipoCocina || 'Cocina general';
                const direccion = restaurante.direccion || '';
                
                const position = [restaurante.lat, restaurante.lon];

                return (
                  <div key={restaurante.id} className="restaurant-result-card-large">
                    
                    <div className="card-large-info">
                      <div className="card-header-row">
                        <h3 
                          className="card-title" 
                          onClick={() => handleShowRestaurantDetailFromFavorites(restaurante)}
                        >
                          {nombre}
                        </h3>
                        
                        <button 
                          className="card-fav-btn-simple active"
                          onClick={() => handleToggleFavorite(restaurante)}
                          title="Eliminar de favoritos"
                        >
                          <FaBookmark />
                        </button>
                      </div>

                      <div 
                        className="card-details-block" 
                        onClick={() => handleShowRestaurantDetailFromFavorites(restaurante)}
                      >
                        <div className="detail-row">
                          <FaUtensils className="detail-icon"/> {cocina}
                        </div>
                        <div className="detail-row">
                          <FaMapMarkerAlt className="detail-icon"/> {ciudad}
                        </div>
                        {direccion && (
                          <div className="detail-row small-text">
                            {direccion}
                          </div>
                        )}
                        <span className="view-more-link">Ver ficha completa</span>
                      </div>
                    </div>

                    <div className="card-large-map">
                      <MapContainer 
                        key={`fav-map-${restaurante.id}`}
                        center={position} 
                        zoom={15} 
                        scrollWheelZoom={false}
                        dragging={false}
                        zoomControl={false}
                        attributionControl={false}
                        style={{ height: '100%', width: '100%' }}
                      >
                        <TileLayer
                          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />
                        <Marker position={position} />
                      </MapContainer>
                    </div>

                  </div>
                );
              })
            )}
          </div>
          
          {favoritesListResults && favoritesListResults.totalElements > 0 && (
            <div className="pagination-controls">
              <button 
                onClick={() => handleFavoritesPagination(favoritesPage - 1)} 
                disabled={favoritesListResults.first}
                className="auth-button"
              >
                Anterior
              </button>
              <span>Página {favoritesPage + 1} de {favoritesListResults.totalPages}</span>
              <button 
                onClick={() => handleFavoritesPagination(favoritesPage + 1)} 
                disabled={favoritesListResults.last}
                className="auth-button"
              >
                Siguiente
              </button>
            </div>
          )}
        </div>
      );
    }

   
    if (view === 'restaurantDetail') {
      if (!selectedRestaurant) {
        return <div className="loading">Cargando restaurante...</div>; 
      }

      const tags = selectedRestaurant.tags || {};
      const nombre = tags.name || 'Nombre no disponible';
      const ciudad = tags['addr:city'] || filterCiudad || 'Ciudad no disponible';
      const direccion = tags['addr:street'] || 'Dirección no disponible';
      const tipoCocina = tags.cuisine || 'Cocina no especificada';
      const position = [selectedRestaurant.lat, selectedRestaurant.lon];

      return (
        <div className="profile-container"> 
          <header className="profile-header-simple">
            <button className="back-button" onClick={handleBackToPreviousView}>
              <FaArrowLeft /> Volver
            </button>
          </header>
          
          <div className="detail-page-content">
            
            <div className="detail-hero">
               <h1>{nombre}</h1>
               <span className="detail-subtitle"><FaMapMarkerAlt/> {ciudad}</span>
            </div>

            <div className="detail-grid">
              
              <div className="detail-box">
                <div className="detail-box-icon"><FaUtensils /></div>
                <div>
                  <small>Tipo de Cocina</small>
                  <strong>{tipoCocina}</strong>
                </div>
              </div>

              <div className="detail-box">
                <div className="detail-box-icon"><FaMapMarkerAlt /></div>
                <div>
                  <small>Dirección</small>
                  <strong>{direccion}</strong>
                </div>
              </div>
              
            </div>

            <div className="detail-map-container">
              <h3>Ubicación</h3>
              <div className="detail-map-frame">
                <MapContainer 
                  center={position} 
                  zoom={16} 
                  style={{ height: '100%', width: '100%' }}
                >
                  <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  />
                  <Marker position={position}>
                    <Popup>{nombre}</Popup>
                  </Marker>
                </MapContainer>
              </div>
            </div>

            <div className="detail-actions">
              <button 
                className="action-button-primary"
                onClick={handleShowCreateReview}
              >
                <FaStar /> Escribir una Reseña
              </button>
            </div>

          </div>
        </div>
      );
    }

  if (view === 'createReview') {
      const nombre = selectedRestaurant?.tags?.name || 'Restaurante';

      return (
        <div className="review-writer-container">
          
          <header className="review-writer-header">
            <button className="back-button-simple" onClick={handleBackToPreviousView}>
              <FaArrowLeft /> Cancelar
            </button>
            <h3>Escribir reseña</h3>
            <div style={{width: '60px'}}></div> 
          </header>

          <div className="review-writer-card">
            
            <div className="review-target-info">
               <h2>{nombre}</h2>
               <p className="review-subtitle">¿Cómo fue tu experiencia?</p>
            </div>

            <form onSubmit={handleSubmitReview}>

              <div className="rating-select-area">
                <div className="stars-row">
                  {[1, 2, 3, 4, 5].map((starValue) => (
                    <FaStar
                      key={starValue}
                      className={`star-input ${starValue <= reviewRating ? 'filled' : ''}`}
                      onClick={() => setReviewRating(starValue)}
                    />
                  ))}
                </div>
                
                <div className="rating-label">
                  {reviewRating === 0 ? 'Toca las estrellas para puntuar' : 
                   reviewRating === 5 ? '¡Excelente!' :
                   reviewRating === 4 ? 'Muy bueno' :
                   reviewRating === 3 ? 'Normal' :
                   reviewRating === 2 ? 'Malo' : 'Terrible'}
                </div>
              </div>

              <textarea
                placeholder="Cuéntanos más sobre la comida, el servicio y el ambiente..."
                className="modern-textarea"
                value={reviewComment}
                onChange={(e) => setReviewComment(e.target.value)}
              />
              
              <div className="photo-upload-section">
                <label className="photo-upload-box">
                  <input
                    type="file"
                    multiple
                    accept="image/*"
                    onChange={handlePhotoSelect}
                    style={{ display: 'none' }}
                  />
                  <div className="upload-placeholder-content">
                    <FaImage className="upload-icon" />
                    <span>Añadir fotos</span>
                    <small>Haz clic para subir imágenes</small>
                  </div>
                </label>
              </div>
              
              {reviewPhotos.length > 0 && (
                <div className="photo-preview-grid">
                  {reviewPhotos.map((file, index) => (
                    <div key={index} className="photo-preview-thumb">
                      <img src={URL.createObjectURL(file)} alt="preview" />
                      <button 
                        type="button" 
                        className="remove-thumb-btn"
                        onClick={() => {
                          setReviewPhotos(prevPhotos => prevPhotos.filter((_, i) => i !== index));
                        }}
                      >
                        <FaTimes />
                      </button>
                    </div>
                  ))}
                </div>
              )}
              
              {error && <p className="error-message" style={{textAlign: 'center'}}>{error}</p>}
              
              <button type="submit" className="action-button-primary full-width" style={{marginTop: '20px'}}>
                Publicar Reseña
              </button>
              
            </form>
          </div>
        </div>
      );
    }

   
    if (view === 'reviewDetail') {
      if (!selectedReview) {
        return <div className="loading">Cargando reseña...</div>;
      }

      const { restaurante, puntuacion, comentario, fotos, nombreUsuario } = selectedReview;

      const isMyReview = profile && profile.nombreUsuario === nombreUsuario;

      return (
        <div className="profile-container">
          <header className="profile-header-simple">
            <button className="back-button" onClick={handleBackToPreviousView}>
              <FaArrowLeft />
            </button>
            <h2 style={{ flex: 1, textAlign: 'left' }}>Reseña de {restaurante.nombre}</h2>
          </header>

          <div className="review-detail-content" style={{ padding: '20px' }}>
            
            <div className="review-stars-container">
              {[1, 2, 3, 4, 5].map((starValue) => (
                <FaStar
                  key={starValue}
                  size={30}
                  color={starValue <= puntuacion ? '#ffc107' : '#e4e5e9'}
                  style={{ margin: '5px' }}
                />
              ))}
            </div>

            {!isMyReview && (
              <p style={{ textAlign: 'center', fontWeight: 'bold', margin: '10px 0' }}>
                Por: {nombreUsuario}
              </p>
            )}

            {comentario && (
              <p style={{ textAlign: 'left', margin: '20px 0', fontSize: '1.1em', whiteSpace: 'pre-wrap' }}>
                {comentario}
              </p>
            )}

            <h3 style={{ textAlign: 'left', borderBottom: '1px solid #ddd', paddingBottom: '5px' }}>Fotos</h3>
            <div className="review-detail-photos">
              {(fotos && fotos.length > 0) ? (
                fotos.map((fotoUrl, index) => (
                  <img 
                    key={index} 
                    src={fotoUrl}
                    alt={`Foto ${index + 1} de la reseña`} 
                    className="review-detail-photo"
                    onClick={() => handleShowImageModal(fotoUrl)}
                  />
                ))
              ) : (
                <p className="info-text" style={{textAlign: 'left'}}>No hay fotos en esta reseña.</p>
              )}
            </div>
            {isMyReview && (
              <button 
                className="auth-button"
                style={{ 
                  backgroundColor: '#c93434',
                  width: '90%', 
                  margin: '30px auto 10px auto' 
                }}
                onClick={handleDeleteReview}
              >
                <FaUserSlash /> Eliminar Reseña
              </button>
            )}
            
          </div>
        </div>
      );
    }

   if (view === 'feed') {
      return (
        <div className="feed-view-container">
          <header className="profile-header-simple">
            <button className="back-button" onClick={handleBackToHome}>
              <FaArrowLeft /> Volver
            </button>
            <h2 style={{ flex: 1, textAlign: 'center', marginRight: '80px' }}>Tu Feed</h2>
          </header>

          <section className="feed-content-wrapper">
            
            {(!feedResults || !feedResults.content || feedResults.content.length === 0) ? (
              <div className="empty-state-container">
                <FaUserPlus className="empty-state-icon" />
                <div className="empty-state-title">Tu feed está tranquilo</div>
                <p className="empty-state-text">
                  Sigue a tus amigos o a otros "foodies" para ver sus reseñas aquí.
                </p>
                <button className="auth-button" style={{marginTop: '20px'}} onClick={handleShowSearch}>
                  Buscar gente para seguir
                </button>
              </div>
            ) : (
              <div className="reviews-list-vertical">
                {feedResults.content.map(valoracion => {
                  if (!valoracion.restaurante) return null;

                  return (
                    <div 
                      key={`${valoracion.restaurante.id}-${valoracion.nombreUsuario}`} 
                      className="feed-card-pro"
                      onClick={() => handleShowReviewDetail(valoracion)}
                    >
                      <div className="feed-pro-header">
                        <div className="feed-user-info">
                           <div className="feed-avatar-placeholder"><FaUser /></div>
                           <div className="feed-header-text">
                              <span className="feed-author-name">{valoracion.nombreUsuario}</span>
                              <span className="feed-action-text">
                                valoró <span className="feed-restaurant-name">{valoracion.restaurante.nombre}</span>
                              </span>
                           </div>
                        </div>
                        <div className="feed-stars-badge">
                          <FaStar className="star-icon" /> {valoracion.puntuacion}/5
                        </div>
                      </div>

                      <div className="feed-pro-body">
                        <div className="feed-comment-text">
                          {valoracion.comentario ? `"${valoracion.comentario}"` : <span style={{color:'#999', fontStyle:'italic'}}>Sin comentario escrito...</span>}
                        </div>
                        
                        {valoracion.fotos && valoracion.fotos.length > 0 && (
                          <div className="feed-pro-photo">
                            <img 
                              src={valoracion.fotos[0]} 
                              alt="Foto reseña" 
                            />
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          {feedResults && feedResults.totalPages > 0 && (
            <div className="pagination-wrapper">
              <button 
                onClick={() => handleFeedPagination(feedPage - 1)} 
                disabled={feedResults.first}
                className="pagination-btn"
              >
                Anterior
              </button>
              <span className="pagination-info">Página {feedPage + 1} de {feedResults.totalPages}</span>
              <button 
                onClick={() => handleFeedPagination(feedPage + 1)} 
                disabled={feedResults.last}
                className="pagination-btn"
              >
                Siguiente
              </button>
            </div>
          )}

        </div>
      );
    }

    

  }; // Fin de renderContent()

  const BottomNavBar = () => (
    <nav className="bottom-nav">
      <button className="nav-button" onClick={handleShowRestauranteSearch}>
        <FaSearch />
      </button>
      <button className="nav-button" onClick={() => handleShowFeed(0)}>
        <FaHome />
      </button>
      <button className="nav-button" onClick={handleShowSearch}>
        <FaUserPlus />
      </button>
      <button className="nav-button" onClick={handleBackToHome}>
        <FaUser />
      </button>
    </nav>
  );

  // --- 9. DEFINICION DE VISTAS DE LA APP ---
  const appViews = [
    'home', 'menu', 'editarPerfil', 'editNombre', 'editEmail', 'editPassword',
    'search', 'userProfile', 'requests', 'userList',
    'filterRestaurantes', 'restaurantResults', 'favoritesList', 'restaurantDetail',
    'createReview', 'reviewDetail', 'feed'
  ];

  // --- 10. RETURN PRINCIPAL DE APP ---
  return (
    <div className="App">
      
      {appViews.includes(view) ? (
        <div className="app-container">
          {renderContent()}
        </div>
      ) : (
        <header className="App-header">
          {renderContent()}
        </header>
      )}

      {appViews.includes(view) && <BottomNavBar />}

      {isImageModalOpen && (
        <div className="image-modal-overlay" onClick={handleCloseImageModal}>
          <img src={modalImageUrl} alt="Vista ampliada" className="image-modal-content" />
        </div>
      )}
    </div>
  );
  
}

export default App;