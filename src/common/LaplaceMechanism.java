package common;
import org.apache.commons.math3.distribution.LaplaceDistribution;


public class LaplaceMechanism {
	
	 static double  addLaplaceNoise(double rawVal, double budget, double sensitivity) throws Exception {
		 
		double scalePara = sensitivity / budget;

		if (scalePara <= 0) {
			throw new Exception("scale Parameter " + scalePara + " for Laplace need >0");
		}

		return new LaplaceDistribution(0, scalePara).sample()+ rawVal;
	}
	
	public static void main(String[] args) {
		//test Apache
		LaplaceDistribution la  = new LaplaceDistribution(0, 1);
		System.out.println(la.sample());
		System.out.println(la.cumulativeProbability(0));
	}

}
