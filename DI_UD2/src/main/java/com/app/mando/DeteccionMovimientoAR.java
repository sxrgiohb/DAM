package com.app.mando;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import javax.swing.*;

public class DeteccionMovimientoAR {
	//Permite invocar a MandoTelevisión para manipular el volumen
	private MandoTelevision mando;
	//Inicializa el objeto DetecciionMovimientoAR y se le asocia con MandoTelevisión.
	public DeteccionMovimientoAR (MandoTelevision mando) {
		this.mando = mando;
	}

	public void controlVolumen() {
		// Inicializamos el convertidor
		OpenCVFrameConverter.ToMat convertidor = new OpenCVFrameConverter.ToMat();
		// Inicializamos la cámara
		VideoCapture captura = new VideoCapture(0);

		if (!captura.isOpened()) {
			System.out.println("No se puede abrir la cámara");
		} else {
			System.out.println("Cámara abierta");
		}

		// Variables para almacenar información de los fotogramas.
		Mat f = new Mat();
		Mat fPrevio = new Mat();
		Mat fDiferencia = new Mat();
		Mat fSuavizado = new Mat();

		// Creamos la pantalla de vídeo.
		CanvasFrame lienzo = new CanvasFrame("Detección de Movimiento", CanvasFrame.getDefaultGamma() / 2.2);
		lienzo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Lee el primer fotograma
		captura.read(fPrevio);
		// Lo pasamos a blanco y negro, ya que no es necesario capturar el video en color.
		opencv_imgproc.cvtColor(fPrevio, fPrevio, opencv_imgproc.COLOR_BGR2GRAY);
		//Constante que vamos a usar para medir el tiempo en el que se va a  imprimir en el video las órdenes de voz.
		final int DURACION_VOZ_MS = 1500;
		
		// Bucle para captura constante de video
		while (lienzo.isVisible() && captura.read(f)) {
			opencv_imgproc.cvtColor(f, f, opencv_imgproc.COLOR_BGR2GRAY);
			opencv_core.absdiff(fPrevio, f, fDiferencia);
			opencv_imgproc.GaussianBlur(fDiferencia, fSuavizado, new Size(5, 5), 0);
			opencv_imgproc.threshold(fSuavizado, fSuavizado, 25, 255, opencv_imgproc.THRESH_BINARY);

			// Encontrar contornos
			MatVector contornos = new MatVector();
			Mat jerarquia = new Mat();
			opencv_imgproc.findContours(fSuavizado, contornos, jerarquia, opencv_imgproc.RETR_EXTERNAL,
					opencv_imgproc.CHAIN_APPROX_SIMPLE);

			// Variables de detección
			boolean brazoIzquierdo = false;
			boolean brazoDerecho = false;

			// Copia del fotograma para dibujar rectángulo
			Mat fRectangulo = f.clone();

			// Dibujar rectángulo en zona con movimiento
			for (int i = 0; i < contornos.size(); i++) {
				Rect rectDelimitador = opencv_imgproc.boundingRect(contornos.get(i));
				if (rectDelimitador.width() < 30 || rectDelimitador.height() < 30) {
					continue;
				}
				// Verificar tamaño y posición del rectángulo respecto a y
				if (rectDelimitador.y() < f.rows() / 2 && rectDelimitador.height() > 100) {
					// Verificamos si está a la derecha o a la izquierda
					if (rectDelimitador.x() < f.cols() / 2) {
						brazoIzquierdo = true;
						opencv_imgproc.rectangle(fRectangulo, rectDelimitador, new Scalar(0, 255, 0, 0));
					} else {
						brazoDerecho = true;
						opencv_imgproc.rectangle(fRectangulo, rectDelimitador, new Scalar(255, 0, 0, 0));
					}
				}
			}
			//Manjeo de gestos e impresión por pantalla
			if (brazoIzquierdo) {
				mando.subirVolumen();
				mando.dibujarVol(fRectangulo, new Scalar (0, 255, 0, 0));
			}
			if (brazoDerecho) {
				mando.bajarVolumen();
				mando.dibujarVol(fRectangulo, new Scalar (255, 0, 0, 0));
			}
			
			//Impresión de órdenes de voz
			//Mientras no haya una orden gestual
			if (!brazoIzquierdo && !brazoDerecho) {
				long tiempoTranscurrido = System.currentTimeMillis() - mando.getVolumenSync();
				//Mientras hay pasado menos del tiempo establecido en la varaible entre el momento actual y el momento de ejecución de la orden.
				if (tiempoTranscurrido < DURACION_VOZ_MS) {
					mando.dibujarVol(fRectangulo, new Scalar(0,0,255,0));
				}
			}
			// Mostrar fotograma con rectángulos y texto
			Frame fMostrar = convertidor.convert(fRectangulo);
			lienzo.showImage(fMostrar);

			// Actualizar fotograma previo si hay movimiento significativo
			if (contornos.size() > 0) {
				fPrevio = f.clone();
			}
		}

	}
}
