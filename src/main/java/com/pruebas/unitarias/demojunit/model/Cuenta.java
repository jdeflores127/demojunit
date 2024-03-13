package com.pruebas.unitarias.demojunit.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Cuenta {
    private String persona;
    private BigDecimal saldo;

    public void debito(BigDecimal bd) {
        BigDecimal nuevoSaldo = saldo.subtract(bd);
        //Se valida que el saldo a debitar es menor o igual al saldo actual de la cuenta
        if (nuevoSaldo.signum() == -1)
            throw new RuntimeException("Saldo insuficiente en la cuenta");
        
        saldo = saldo.subtract(bd);
    }

    public void credito(BigDecimal bd) {
        saldo = saldo.add(bd);
    }
}
