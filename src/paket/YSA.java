package paket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

import  java.util.List;
import java.util.Random;

public class YSA {
	
	private static final File inputFile = new File(YSA.class.getResource("input.txt").getPath());
	private static final File egitimDosya = new File(YSA.class.getResource("Egitim.txt").getPath());
	private static final File testDosya = new File(YSA.class.getResource("Test.txt").getPath());
	
	private double[] maksimumlar;
	private double[] minimumlar;
	
	private DataSet egitimVeriSeti;
	private DataSet testVeriSeti;
	private int araKatmanNoronSayisi;
	MomentumBackpropagation mbp;
	BackPropagation bp;
	double []eldeEdilenHatalar;
	double epoch;
	
	
	public YSA(int araKatmanNoronSayisi, double momentum, double ogrenmeKatsayisi, double hata,int epoch) throws IOException
	{
		maksimumlar = new double[3];//hem input hem output normalize edileceðinden (2 + 1)
		minimumlar = new double[3];
		VeriSetiAyir(inputFile);
		
		
		for(int i=0; i<3;i++) {
			maksimumlar[i]= Double.MIN_VALUE;
			minimumlar[i]= Double.MAX_VALUE;
		}
		VeriSetiMinMax(egitimDosya);
		VeriSetiMinMax(testDosya);
		egitimVeriSeti = VeriSetiOku(egitimDosya);
		testVeriSeti  = VeriSetiOku(testDosya);
		
		//ogrenmeKatsayisi ve epoch ikisi de veriliyor once hangisi durdurursa training
		mbp = new MomentumBackpropagation();
		mbp.setMomentum(momentum);
		mbp.setLearningRate(ogrenmeKatsayisi);
		mbp.setMaxError(hata);
		mbp.setMaxIterations(epoch);
		
		bp = new BackPropagation();
		bp.setLearningRate(ogrenmeKatsayisi); 
		bp.setMaxIterations(epoch);	
		bp.setMaxError(hata);
		eldeEdilenHatalar = new double[(int)epoch];
		
		this.epoch = epoch;
		this.araKatmanNoronSayisi = araKatmanNoronSayisi;
		
	}
	
	//egitim hatasini dondurur
	public double egitimHata(int networkType) {
		if(networkType == 1) return mbp.getTotalNetworkError();
		else return bp.getTotalNetworkError();
	}
	
	
	
	//Rastgele bir sekilde test ve egitim verisini ayirir
	private void VeriSetiAyir(File file) throws IOException {
		Random rand = new Random();
		//Test data indexlerini rastgele uretip diziye atýyor
		int []testIndeksler = new int[300];
		for(int i = 0; i < 300; i++) {
			testIndeksler[i] = rand.nextInt(1000);
			//Diðerleri ile ayný olmamasi kontrolu
			if(i > 0) {
				for(int j = i-1; j>=0; j--) {
					if(testIndeksler[i] == testIndeksler[j]) {
						testIndeksler[i] = rand.nextInt(1000);
						j=i-1;
					}
				}
			}
		}
		Scanner scan = new Scanner(file);
		int satirNumarasiCounter = 1;
		
		PrintWriter writer = new PrintWriter(egitimDosya);
		writer.print("");
		writer.close();
		PrintWriter writer2 = new PrintWriter(testDosya);
		writer2.print("");
		writer2.close();
		
		while(scan.hasNextLine()) {
			String okunan = scan.nextLine();
			
			//Satir numarasi olusturulan rastgele sayilar arasinda varsa test.txt dosyasina yoksa egitim dosyasina yazdirir
			boolean varMi = false;
			for(int i = 0; i < testIndeksler.length; i++) {
				if(satirNumarasiCounter == testIndeksler[i]) {
					varMi = true;
					break;
				}
			}
			
			if(varMi) {
				BufferedWriter writerTest = new BufferedWriter(new FileWriter(testDosya, true));
				writerTest.append(okunan + "\n");
				writerTest.close();
			} else {
				BufferedWriter writerEgitim = new BufferedWriter(new FileWriter(egitimDosya, true));
				writerEgitim.append(okunan + "\n");
				writerEgitim.close();
			}
			satirNumarasiCounter++;
		}
		scan.close();
	}
	
	
	
	
	private double mse(double []beklenen, double []cikti) {	//hata hesap yontemlerinden mse (beklenen-cikti)^2
		
		return Math.pow(beklenen[0]-cikti[0], 2);	
	}
	
	//ortalama test hatasýný buluyor
	//satýrlar tek tek aga verilir. sadece ileri beslemeyapar. hata hesaplanir
	public double testHata(int networkType) {	
		NeuralNetwork sinirselAg;
		if(networkType == 1) sinirselAg = NeuralNetwork.createFromFile("mbpOgrenenAg.nnet");
		else sinirselAg = NeuralNetwork.createFromFile("bpOgrenenAg.nnet");
		
		double toplamHata = 0;
		List<DataSetRow> satirlar = testVeriSeti.getRows(); //satýr satýr dolaþabilelim diye
		for(DataSetRow dr: satirlar) {
			sinirselAg.setInput(dr.getInput());
			sinirselAg.calculate();
			toplamHata += mse(dr.getDesiredOutput(),sinirselAg.getOutput());			
		}
		return toplamHata/testVeriSeti.size();
	}
	
	
	//Minimum degerleri bulunduran diziyi donduren fonksiyon
	public double []getMinimumlar(){
		return this.minimumlar;
	}
	
	
	//Maksimum degerleri bulunduran diziyi donduren fonksiyon
	public double []getMaksimumlar(){
		return this.maksimumlar;
	}
	
	//Normalize outputu gercek hayat datasýna donusturuyor
	private double Sonuc(double output) {
		return (output * (this.getMaksimumlar()[2] - this.getMinimumlar()[2]) + this.getMinimumlar()[2]);
	}
	
	//sonucu normalize sekilde uretiyor. Ama kullanýcýnýn anlayacagi denormalize Sonuc fonksiyonundan geleni donduruyor.
	public double tekTest(double[] inputs, int networkType) {
		
		//Tekrar calistiginda normalize edilmis inputlarý tekrar normalize etmemesi için gerçek inputlarý geçici deðiþkende tutuyorum
		double[] tempInputs = new double[inputs.length] ;
		for(int i=0; i<inputs.length; i++) {
			tempInputs[i] = inputs[i];
		}
		
		for(int i=0; i<2; i++) {
			inputs[i] = minMax(maksimumlar[i],minimumlar[i], inputs[i]);
		}
		
		NeuralNetwork sinirselAg;
		
		if (networkType == 1) { //yani aðýmýz mbp ise
			 sinirselAg = NeuralNetwork.createFromFile("mbpOgrenenAg.nnet");
		}
		else {
			 sinirselAg = NeuralNetwork.createFromFile("bpOgrenenAg.nnet");
		}
		
		sinirselAg.setInput(inputs);
		sinirselAg.calculate();
		
		//inputlar dizisine normalize edilmemiþ deðerleri tekrar atýyorum.
		for(int i=0; i<inputs.length; i++) {
			inputs[i] = tempInputs[i];
		}

		return Sonuc(sinirselAg.getOutput()[0]);
	}
	
	private double minMax(double max, double min, double x) {
		return (x-min)/(max-min);
	}
	
	private DataSet VeriSetiOku(File file) throws FileNotFoundException {
		Scanner scan = new Scanner(file);
		DataSet dataset = new DataSet(2,1); //2 input 1 outputlu bir dataset olustur
		
		while(scan.hasNextDouble()) {	
			double []inputs = new double[2];
			
			for(int i=0;i<2;i++) {
				double d = scan.nextDouble();
				inputs[i]= minMax(this.maksimumlar[i],this.minimumlar[i],d);
			}
			
			//Burada ayri ayri gelen input output datasetlerini birlestirdik:
			double output = minMax(this.maksimumlar[2], this.minimumlar[2], scan.nextDouble());
			dataset.add(new DataSetRow(inputs, new double[] {output}));			
		}
		scan.close();
		return dataset;
	}
	
	public void mbpEgit() {
		MultiLayerPerceptron sinirselAg = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,2,araKatmanNoronSayisi,1);
		sinirselAg.setLearningRule(mbp);
		sinirselAg.learn(egitimVeriSeti);
		sinirselAg.save("mbpOgrenenAg.nnet");
		System.out.println("Momentumlu BP Egitimi Tamamlandi");
	}
	
	public void bpEgit() throws FileNotFoundException {
		
		//Ilk parametresi transfer fonksiyonu: aktivasyon fonksiyonu. biz desteklenenlerden sigmoidi seçtik
		MultiLayerPerceptron sinirselAg = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,2,araKatmanNoronSayisi,1); 
		sinirselAg.setLearningRule(bp);

		//Egitimi epoch epoch calistiriyoruz. Hatalari diziye alýyoruz grafik çizebilmek icin
		for (int i = 0; i < epoch; i++) {
			sinirselAg.getLearningRule().doOneLearningIteration(egitimVeriSeti);
			if(i==0) eldeEdilenHatalar[i] = 1;
			else eldeEdilenHatalar[i] = sinirselAg.getLearningRule().getPreviousEpochError();
		}
		
		sinirselAg.save("bpOgrenenAg.nnet");
		System.out.println("Momentumsuz BP Egitimi tamamlandi.");
	}
	
	public double[] hatalar() {
		return eldeEdilenHatalar;
	}
	
	private void VeriSetiMinMax(File file) throws FileNotFoundException {
		Scanner scan = new Scanner(file);
	
		while(scan.hasNextDouble()) {
			for(int i=0;i<3;i++) { //ilk 8 input diye // ben hem input hem outputu normalize ettim burda?
				double d = scan.nextDouble();
				
				if(d>maksimumlar[i]) maksimumlar[i]=d;
				if(d<minimumlar[i]) minimumlar[i]=d;
			}
		}
		
		scan.close();
	}
	
	
	
}
