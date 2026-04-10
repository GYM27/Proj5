package aor.paj.projecto5.utils;

public enum UserRoles {
    ADMIN(1),
    NORMAL(2);

    private final int uRoleId;

    UserRoles(int uRoleId){
        this.uRoleId=uRoleId;
    }

    public int getURoleId(){
        return this.uRoleId;
    }

    /**
     * Recebe um id numérico e devolve o respetivo userRole.
     * @param id id numérico correspondente ao userRole
     * @return o userRole que queremos
     */
    public static UserRoles getUserRolesFromId(int id){
        for(UserRoles role:values()){
            if(role.uRoleId==id){
                return role;
            }
        }
        return  null;
    }

}
