package com.app.mando;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

public class MandoTelevision {
	private int volumen;
	// Variable que vamos a usar para comunicar entre hilos.
	private long volumenSync;

	// Constructor
	public MandoTelevision() {
		this.volumen = 0;
		this.volumenSync = 0;
	}

	// Método para subir el volumen
	public void subirVolumen() {
		if (this.volumen < 100) {
			this.volumen++;
			this.volumenSync = System.currentTimeMillis();
		} else {
			System.out.println("Volumen al máximo");
		}
	}

	// Método para bajar el volumen
	public void bajarVolumen() {
		if (this.volumen > 0) {
			this.volumen--;
			this.volumenSync = System.currentTimeMillis();
		} else {
			System.out.println("El volumen ya está en 0 y no se puede bajar más.");
		}
	}

	// Método para obtener el volumen actual
	public int obtenerVolumen() {
		return this.volumen;
	}

	// Método para obtener la sincronización
	public long getVolumenSync() {
		return this.volumenSync;
	}

	// Método para dibujar el volumen en el vídeo
	public void dibujarVol(Mat frame, Scalar color) {
		opencv_imgproc.putText(frame, "Volumen: " + volumen, new Point(10, 60), opencv_imgproc.FONT_HERSHEY_SIMPLEX,
				1.0, color, 2, opencv_imgproc.LINE_AA, false);
	}

}
