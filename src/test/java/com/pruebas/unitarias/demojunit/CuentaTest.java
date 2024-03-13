package com.pruebas.unitarias.demojunit;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.pruebas.unitarias.demojunit.model.Banco;
import com.pruebas.unitarias.demojunit.model.Cuenta;

class CuentaTest {

    Cuenta cuenta;
    Cuenta cuenta2;

    TestInfo testInfo;
    TestReporter testReporter;
    
    @BeforeEach
    void initCuenta(TestInfo testInfo, TestReporter testReporter) {
        this.testInfo=testInfo;
        this.testReporter=testReporter;
        
        testReporter.publishEntry("Nombre Clase: "+ testInfo.getTestClass().get().getSimpleName()+", Descripcion: "+testInfo.getDisplayName()+", NombreMetodo:"+testInfo.getTestMethod().get().getName());
        cuenta = Cuenta.builder().persona("John").saldo(new BigDecimal("700.20")).build();
        cuenta2 = Cuenta.builder().persona("John").saldo(new BigDecimal("700.20")).build();
        
    }

    @AfterEach
    void finalizarPrueba() {
        System.out.println("Prueba finalizada");
    }

    @BeforeAll
    static void beforeClass() {
        System.out.println("se ejecuta clase CuentaTest All");
    }

    @AfterAll
    static void afterClass() {
        System.out.println("se ejecuta clase CuentaTest All");
    }

    @Nested
    class TestCuentaNombreSaldo {
        @Test
        //@Disabled
        @DisplayName("Se prueba que la cuenta realice bien el getter y setter de persona")
        void testNombreCuenta() {
            Cuenta cuenta = Cuenta.builder().persona("Jair").build();

            String esperado = "Jair";
            String real = cuenta.getPersona();
            
            assertThat(real).isEqualTo(esperado);
            assertTrue(esperado.equals(real));
        }

        @Test
        void testsaldoCuenta() {

            Cuenta cuenta = Cuenta.builder()
                    .persona("Jair")
                    .saldo(new BigDecimal("500.00"))
                    .build();

            assertThat(cuenta.getSaldo()).isEqualTo(new BigDecimal("500.00"));
            assertThat(cuenta.getSaldo()).isGreaterThan(new BigDecimal("0"));
        }

        @Test
        void testReferenciaCuenta() {
            assertThat(cuenta).isEqualTo(cuenta2);

        }

        @Test
        void testDebitoCuenta() {
            BigDecimal saldoInicial = cuenta.getSaldo();
            BigDecimal debito = new BigDecimal(200);
            BigDecimal saldoEsperado = saldoInicial.subtract(debito);

            cuenta.debito(debito);

            assertThat(saldoInicial).isNotNull();
            assertThat(saldoEsperado).isEqualTo(cuenta.getSaldo());
        }

        @Test
        void testCreditoCuenta() {
            BigDecimal saldoInicial = cuenta.getSaldo();
            BigDecimal debito = new BigDecimal(200);
            BigDecimal saldoEsperado = saldoInicial.add(debito);

            cuenta.credito(debito);
            assertThat(saldoInicial).isNotNull();
            assertThat(saldoEsperado).isEqualTo(cuenta.getSaldo());
        }
    }

    @Nested
    class testDebito {
        @Test
        void testDineroInsuficienteDebito() {
            String mensajeError;
            BigDecimal debito = new BigDecimal("800");
            // Se valida si el debito es mas grande
            Exception excepcion = assertThrows(RuntimeException.class, () -> cuenta.debito(debito));
            mensajeError = excepcion.getMessage();

            assertEquals(mensajeError, "Saldo insuficiente en la cuenta", () -> "el saldo no se esperaba");
        }

        @Test
        void testTransferirDineroCuenta() {

            cuenta2.setSaldo(new BigDecimal("3000"));

            BigDecimal montoTransferencia = new BigDecimal("2000");
            BigDecimal montoCuenta1 = cuenta.getSaldo().add(montoTransferencia);
            BigDecimal montoCuenta2 = cuenta2.getSaldo().subtract(montoTransferencia);

            Banco banco = Banco.builder()
                    .nombre("BBVA").build();

            banco.transferir(cuenta, cuenta2, montoTransferencia);
            // Cuando tenemos Varios asserts podemos agruparlos en un assertAll

            assertAll(
                    () -> assertThat(cuenta.getSaldo()).isEqualTo(montoCuenta1),
                    () -> assertThat(cuenta2.getSaldo()).isEqualTo(montoCuenta2));

            // Se valida que a la primera cuenta se le hayan depositado 2000
            // Se valida que a la segunda cuenta se le hayan retirado 2000
        }

        @Test
        void testTransferirDineroCuentaDatosInvalidos() {
            cuenta2.setSaldo(new BigDecimal("3000"));

            BigDecimal montoCuenta2 = new BigDecimal("-1000");

            Banco banco = Banco.builder()
                    .nombre("BBVA").build();

            assertThrows(RuntimeException.class, () -> banco.transferir(null, cuenta2, montoCuenta2));
            assertThrows(RuntimeException.class, () -> banco.transferir(cuenta, null, montoCuenta2));
            assertThrows(RuntimeException.class, () -> banco.transferir(null, cuenta2, montoCuenta2));
            assertThrows(RuntimeException.class, () -> banco.transferir(cuenta, cuenta2, null));
        }

        @Test
        void testRelacionBancoCuentas() {
            cuenta2.setSaldo(new BigDecimal("3000"));

            Banco banco = Banco.builder()
                    .nombre("BBVA").build();

            banco.agregarCuentaHabiente(cuenta);
            banco.agregarCuentaHabiente(cuenta2);

            assertThat(banco.getCuentaHabientes()).asList().containsAll(Arrays.asList(cuenta, cuenta2));

        }
    }

    @Nested
    class EnvironmentTest {
        @Test
        @EnabledOnOs({ OS.WINDOWS })
        void soloWindows() {
            System.out.println("Test solo se ejecuta en windows");

        }

        @Test
        @EnabledOnOs({ OS.MAC })
        void soloMac() {
            System.out.println("Test solo se ejecuta en MAC");

        }

        @Test
        @DisabledOnOs({ OS.MAC })
        void noEjecucionMac() {
            System.out.println("No se ejecutará en MAC");

        }

        @Test
        @EnabledOnJre({ JRE.JAVA_17 })
        void ejecucionJava17() {
            System.out.println("se ejecutara en java 17");

        }

        @Test
        @EnabledIfSystemProperty(named = "file.encoding", matches = "UTF-8")
        void imprimirProperties() {
            Properties propList = System.getProperties();
            propList.forEach((clave, valor) -> System.out.println("clave: " + clave + " | valor: " + valor));

        }

        @Test
        @EnabledIfEnvironmentVariable(named = "HOME", matches = "/(.)*")
        void imprimirVariablesEntorno() {
            Map<String, String> entorno = System.getenv();
            entorno.forEach((clave, valor) -> System.out.println("clave: " + clave + " | valor: " + valor));
        }
    }

    @Nested
    class TestAssumptions {
        @Test
        @DisplayName("Valida que el servidor de desarrollo se encuentre activo y despues valida la respuesta," +
                "si el servicio esta abajo la prueba se ignorará")
        void pruebaConexionServidor() {
            Boolean simulaConexion = false;
            assumeTrue(simulaConexion);

            Cuenta simulaRespuestaServidor = cuenta;

            System.out.println("servidor activo, se continua con la prueba");
            assertEquals("John", cuenta.getPersona());

        }

        @Test
        @DisplayName("Valida que el ambiente sea DEV, si es así ejecutara la validación de nombre, " +
                "En caso contrario, se ignorará la validacion de nombre y se ejecutará la validacion de monto")
        void pruebaAmbiente() {
            Boolean ambienteDesarrollo = true;
            // Si es false ambienteDesarrollo, lo que haya dentro de la funcion no se
            // ejecutará
            assumingThat(ambienteDesarrollo, () -> {
                assertEquals("John", cuenta.getPersona());
                System.out.println("Validacion de nombre exitosa");
            });

            assertThat(cuenta.getSaldo()).isGreaterThan(new BigDecimal("200"));
            System.out.println("validacion de precio exitosa");
        }
    }

    @Nested
    class TestConRepeticiones {
        @RepeatedTest(value = 5, name = "Repeticion {currentRepetition} de {totalRepetitions}")
        @DisplayName("Prueba que se repite 5 veces")
        void testRepite5veces(RepetitionInfo info) {
            info.getCurrentRepetition();
            assertTrue(true);
            System.out.println("Prueba exitosa");
        }

        @ParameterizedTest(name = "numero {index} ejecutado con valor {0} - {argumentsWithNames}")
        @ValueSource(strings = { "100", "200", "500", "1000", "5000" })
        void testTransferirDineroCuentaParamTest(String monto) {

            cuenta2.setSaldo(new BigDecimal("3000"));

            BigDecimal montoTransferencia = new BigDecimal(monto);
            BigDecimal montoCuenta1 = cuenta.getSaldo().add(montoTransferencia);
            BigDecimal montoCuenta2 = cuenta2.getSaldo().subtract(montoTransferencia);

            Banco banco = Banco.builder()
                    .nombre("BBVA").build();

            banco.transferir(cuenta, cuenta2, montoTransferencia);
            // Cuando tenemos Varios asserts podemos agruparlos en un assertAll

            assertAll(
                    () -> assertThat(cuenta.getSaldo()).isEqualTo(montoCuenta1),
                    () -> assertThat(cuenta2.getSaldo()).isEqualTo(montoCuenta2));

            // Se valida que a la primera cuenta se le hayan depositado 2000
            // Se valida que a la segunda cuenta se le hayan retirado 2000
        }
    }

    @Nested
    @Tag("parametrizedTest")
    class ParamTest {
        @ParameterizedTest(name = "numero {index} ejecutado con valor {0} - {argumentsWithNames}")
        @CsvSource({ "1,100", "2,200", "3,500", "4,1000", "5,5000" })
        void testTransferirDineroCuentaParamTestCsv(String index, String monto) {

            cuenta2.setSaldo(new BigDecimal("3000"));

            BigDecimal montoTransferencia = new BigDecimal(monto);
            BigDecimal montoCuenta1 = cuenta.getSaldo().add(montoTransferencia);
            BigDecimal montoCuenta2 = cuenta2.getSaldo().subtract(montoTransferencia);

            Banco banco = Banco.builder()
                    .nombre("BBVA").build();

            banco.transferir(cuenta, cuenta2, montoTransferencia);
            // Cuando tenemos Varios asserts podemos agruparlos en un assertAll

            assertAll(
                    () -> assertThat(cuenta.getSaldo()).isEqualTo(montoCuenta1),
                    () -> assertThat(cuenta2.getSaldo()).isEqualTo(montoCuenta2));

            // Se valida que a la primera cuenta se le hayan depositado 2000
            // Se valida que a la segunda cuenta se le hayan retirado 2000
        }

        @ParameterizedTest(name = "numero {index} ejecutado con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void testTransferirDineroCuentaParamTestCsvFile(String monto, String persona) {

            System.out.println("monto: " + monto + " persona: " + persona);
            cuenta2.setSaldo(new BigDecimal("9000"));
            cuenta.setPersona(persona);

            BigDecimal montoTransferencia = new BigDecimal(monto);
            BigDecimal montoCuenta1 = cuenta.getSaldo().add(montoTransferencia);
            BigDecimal montoCuenta2 = cuenta2.getSaldo().subtract(montoTransferencia);

            Banco banco = Banco.builder()
                    .nombre("BBVA").build();

            banco.transferir(cuenta, cuenta2, montoTransferencia);
            // Cuando tenemos Varios asserts podemos agruparlos en un assertAll

            assertAll(
                    () -> assertThat(cuenta.getSaldo()).isEqualTo(montoCuenta1),
                    () -> assertThat(cuenta2.getSaldo()).isEqualTo(montoCuenta2));

        }

        @ParameterizedTest(name = "numero {index} ejecutado con valor {0} - {argumentsWithNames}")
        @MethodSource("montoListMethod")
        void testTransferirDineroCuentaParamTestMethod(String monto) {

            cuenta2.setSaldo(new BigDecimal("3000"));

            BigDecimal montoTransferencia = new BigDecimal(monto);
            BigDecimal montoCuenta1 = cuenta.getSaldo().add(montoTransferencia);
            BigDecimal montoCuenta2 = cuenta2.getSaldo().subtract(montoTransferencia);

            Banco banco = Banco.builder()
                    .nombre("BBVA").build();

            banco.transferir(cuenta, cuenta2, montoTransferencia);
            // Cuando tenemos Varios asserts podemos agruparlos en un assertAll

            assertAll(
                    () -> assertThat(cuenta.getSaldo()).isEqualTo(montoCuenta1),
                    () -> assertThat(cuenta2.getSaldo()).isEqualTo(montoCuenta2));

        }

        static List<String> montoListMethod() {
            return Arrays.asList("1", "2", "100", "200", "1000", "2000");

        }
    }

    @Nested
    @Tag("timeout")
    class TestTimeOut{
        
        @Test
        @Timeout(value = 1000, unit = TimeUnit.MICROSECONDS)
        void pruebaTimeOut() throws InterruptedException{
            TimeUnit.MILLISECONDS.sleep(1100);
        }
        
        @Test
        void pruebaTimeOutAssertions(){
            assertTimeout(Duration.ofSeconds(5), ()->{
                TimeUnit.MILLISECONDS.sleep(1100);
            });
        }
    }

}