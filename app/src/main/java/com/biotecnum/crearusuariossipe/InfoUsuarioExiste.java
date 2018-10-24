package com.biotecnum.crearusuariossipe;

/**
 * Created by ingluismb on 29/10/17.
 */

public class InfoUsuarioExiste {
    public String nombre;
    public String cedula;
    public String empresa;
    public int pasajes;

    public InfoUsuarioExiste(){

    }

    public InfoUsuarioExiste(String nombre, String cedula, String empresa, int pasajes){
        this.nombre = nombre;
        this.cedula = cedula;
        this.empresa = empresa;
        this.pasajes = pasajes;
    }

}
