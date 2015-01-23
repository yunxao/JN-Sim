package tid.utils;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import tid.graphic.GraphicInterpreter;

public class CargadorDeImagenes{

	public static BufferedImage getImagen(String file){
		BufferedImage imagen = null;	
		//Le pedimos al ClassLoader que busque en el jar la url de nuestra imagen o recurso
		java.net.URL imageURL = GraphicInterpreter.class.getClassLoader().getResource("/node0.png");
		System.out.println(imageURL);
		try{		
			imagen = ImageIO.read(imageURL);
		}catch(java.io.IOException e){
			System.out.println(e.getMessage());
		}
		return imagen;
	}

	}
