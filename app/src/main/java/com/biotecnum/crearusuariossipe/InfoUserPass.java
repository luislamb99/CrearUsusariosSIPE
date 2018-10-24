package com.biotecnum.crearusuariossipe;

/**
 * Created by ingluismb on 26/10/17.
 */

public class InfoUserPass {
    public String cargo;
    public String celular;
    public String nombre;
    public String password;
    public String ubicacion;
    public boolean sancion;
    public int numPasajes;
    public int numTarjetas;

    public InfoUserPass() {
        // Default constructor required for calls to DataSnapshot.getValue(infoConductor.class)
    }

    public InfoUserPass(String cargo, String celular, String nombre, String password, String ubicacion, boolean sancion, int numPasajes, int numTarjetas) {
        this.cargo = cargo;
        this.celular = celular;
        this.nombre = nombre;
        this.password = password;
        this.ubicacion = ubicacion;
        this.sancion = sancion;
        this.numPasajes = numPasajes;
        this.numTarjetas = numTarjetas;

    }
}
