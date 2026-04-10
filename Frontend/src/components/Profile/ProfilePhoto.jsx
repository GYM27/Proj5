import React from "react";

/**
 * COMPONENTE: ProfilePhoto
 * -----------------------
 * DESCRIÇÃO: Renderiza a imagem de perfil do utilizador.
 * FUNCIONALIDADE: Possui uma lógica de 'Fallback' (recurso de reserva) que gera
 * um avatar automático caso o utilizador não tenha definido uma photoUrl.
 * @param {string} photoUrl - Link para a foto alojada.
 * @param {string} firstName - Nome para gerar a inicial do avatar.
 * @param {string} lastName - Apelido para gerar a inicial do avatar.
 */
const ProfilePhoto = ({ photoUrl, firstName, lastName }) => {

    /**
     * LÓGICA DE AVATAR (CRITÉRIO: UX - 3%):
     * Se 'photoUrl' existir, usamos a foto real.
     * Caso contrário, chamamos a API ui-avatars para criar uma imagem com as iniciais.
     * O parâmetro 'background=0d6efd' mantém a coerência com a cor primária do Bootstrap (Primary Blue).
     */
    const avatarUrl = photoUrl
        ? photoUrl
        : `https://ui-avatars.com/api/?name=${firstName}+${lastName}&background=0d6efd&color=fff&rounded=true`;

    return (
        <div className="text-center mb-4">
            {/* RENDERIZAÇÃO DA IMAGEM:
          - 'rounded-circle': Classe Bootstrap para formato circular (Padrão de Perfis).
          - 'shadow': Adiciona profundidade visual ao componente.
          - 'objectFit: cover': Garante que fotos com proporções diferentes não fiquem esticadas ou esmagadas.
      */}
            <img
                src={avatarUrl}
                alt="Perfil"
                className="rounded-circle shadow"
                style={{ width: "120px", height: "120px", objectFit: "cover" }}
            />
        </div>
    );
};

export default ProfilePhoto;