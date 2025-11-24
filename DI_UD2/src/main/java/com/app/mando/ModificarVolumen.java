package com.app.mando;

import java.io.IOException;

import org.bytedeco.javacv.FrameGrabber;

public class ModificarVolumen {
	public static void main(String[] args) throws FrameGrabber.Exception {

		MandoTelevision mando = new MandoTelevision();
		DeteccionMovimientoAR ar = new DeteccionMovimientoAR(mando);
		MandoVoz voz = new MandoVoz(mando);

		// Creamos el hilo para el control mediante gestos.
		Thread hiloAR = new Thread(() -> {
			try {
				System.out.println("Iniciando detección de movimiento");
				ar.controlVolumen();
			} catch (Exception e) {
				//Error en el hilo.
				System.out.println("Error en detección de movimiento");
				e.printStackTrace();
			}
		});

		// Creamos el hilo para el control mediante voz.
		Thread hiloVoz = new Thread(() -> {
			try {
				System.out.println("Iniciando detección de voz");
				voz.escucharInstrucciones();
			} catch (IOException e) {
				//Error de entrada y salida.
				System.out.println("Error IO de voz");
				e.printStackTrace();
			} catch (Exception e) {
				//Error en el hilo.
				System.out.println("Error en detección de voz");
				e.printStackTrace();
			}
		});
		//Iniciamos los hilos.
		hiloAR.start();
		hiloVoz.start();
	}
}
