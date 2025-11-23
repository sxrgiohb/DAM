package com.app.mando;

//Vosk para el reconocimiento y procesamiento del audio mediante un modelo determinado.
import org.vosk.Model;
import org.vosk.Recognizer;

//Permite usar la entrada de sonido del dispositivo.
import javax.sound.sampled.*;
//Maneja errores de entrada y salida
import java.io.IOException;

public class MandoVoz {
	//Permite invocar a MandoTelevisión para manipular el volumen
    private MandoTelevision mando;
    
    //Inicializa el objeto MandoVoz y se le asocia con MandoTelevisión
    public MandoVoz(MandoTelevision mando) {
        this.mando = mando;
    }
    //Método para iniciar la escucha.
    public void escucharInstrucciones() throws IOException, LineUnavailableException {
    	//Modelo descargado para el reconocimiento de voz
        Model model = new Model("RUTA AL MODELO VOSK");
        //Definimos cómo vamos a capturar el audio.
        AudioFormat formato = new AudioFormat(44100, 16, 1, true, false);
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, formato);
                
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Micrófono no soportado");
            model.close();
            return;
        }
        //Micrófono
        TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
        //Abre el micrófono con el formato especificado.
        mic.open(formato);
        //Empieza la captura de sonido.
        mic.start();
        
        //Se crea el reconocedor de sonido.
        Recognizer recognizer = new Recognizer(model, 44100);
        
        //Array que almacena las muestras del micrófono
        byte[] buffer = new byte[4096];
        try {
	        while (true) {
	        	//lee continuamente bloques de audio.
	            int bytesLeidos = mic.read(buffer, 0, buffer.length);
	            //Si el reconocedor detecta una frase la pasa como string al resultado.
	            if (recognizer.acceptWaveForm(buffer, bytesLeidos)) {
	                String resultado = recognizer.getResult();
	                //Sube el volumen con la orden verbal.
	                if (resultado.contains("subir volumen")) {
	                    mando.subirVolumen();
	                    System.out.println("Comando recibido: subir volumen\nVolumen actual: " + mando.obtenerVolumen());
	                //Baja el volumen con la orden verbal.
	                } else if (resultado.contains("bajar volumen")) {
	                    mando.bajarVolumen();
	                    System.out.println("Comando recibido: bajar volumen\\nVolumen actual: " + mando.obtenerVolumen());
	                }
	            }
	        }
	        
        }  finally {
        	//Se cierran los recursos
        	model.close();
        	mic.stop();
        	mic.close();
        	recognizer.close();
        }
    }
}