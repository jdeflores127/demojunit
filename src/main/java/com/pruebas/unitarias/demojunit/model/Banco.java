package com.pruebas.unitarias.demojunit.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Banco {
    private String nombre;
    @Builder.Default
    private List<Cuenta> cuentaHabientes = new ArrayList<>();
    
    public void agregarCuentaHabiente(Cuenta cuenta){
        cuentaHabientes.add(cuenta);
    }
    public void transferir(Cuenta cuentaBeneficiaria, Cuenta cuentaPagante, BigDecimal monto){
        if(cuentaBeneficiaria == null )
            throw new RuntimeException("Cuenta Beneficiaria no puede ser null");
        if(cuentaPagante == null )
            throw new RuntimeException("Cuenta Pagante no puede ser null");
        if(monto!=null && monto.signum() == -1 )
            throw new RuntimeException("monto de transferencia debe ser mayor a 0");
        
        cuentaBeneficiaria.credito(monto);
        cuentaPagante.debito(monto);

    }
}
