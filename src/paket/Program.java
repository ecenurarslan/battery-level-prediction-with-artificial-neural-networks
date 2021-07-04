package paket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Program {

	public static void main(String[] args) throws IOException {

		Scanner in = new Scanner(System.in);
		int araKatmanNoronSayi = 10;
		double momentum = 0.9, ogrenmeKatsayisi = 0.1, hata = 0.0001;
		int epoch = 100, sec=0;
		YSA MBP = null;
		YSA BP = null;
		
		BP= new YSA(araKatmanNoronSayi,0,ogrenmeKatsayisi,hata,epoch);
		BP.bpEgit();
		System.out.println("BP Egitimde Elde Edilen Hata: "+ BP.egitimHata(2));	//bir verirsen mbp ninkini dondurur
		System.out.println("BP Testte Elde Edilen Hata: "+ BP.testHata(2) + "\n");
		
		MBP= new YSA(araKatmanNoronSayi,momentum,ogrenmeKatsayisi,hata,epoch);
		MBP.mbpEgit();
		System.out.println("MBP Egitimde Elde Edilen Hata: "+ MBP.egitimHata(1));	//bir verirsen mbp ninkini dondurur
		System.out.println("MBP Testte Elde Edilen Hata: "+ MBP.testHata(1) + "\n");
		
		do {
			System.out.println("1. Algoritmalarý Test Et");
			System.out.println("2. Cikis");
			System.out.println("=>");
			sec = in.nextInt();
			
			switch(sec) {
				
				case 1:
					//nesneler olusturulmamýs ise egitimler de yapilmamistir kontrolu
					if(BP == null) {	
						System.out.println("BP Egitimi Yapilmamis");
						System.in.read();
						break;
					}
					if(MBP == null) {	
						System.out.println("MBP Egitimi Yapilmamis");
						System.in.read();
						break;
					}
					double []inputs = new double[2];
					System.out.println("Voltaj (11.5 - 13.5): ");
					inputs[0]= in.nextDouble();
					System.out.println("Sicaklik (26 - 36): ");
					inputs[1]= in.nextDouble();
					
					System.out.println("Back Propagation ile Hesaplanan Pil Doluluk Durumu: "+BP.tekTest(inputs,2));	//2 verdik = bp demek
					System.out.println("Momentum Back Propagation ile Hesaplanan Pil Doluluk Durumu: "+MBP.tekTest(inputs,1));	//1 verdik = mbp demek
					System.in.read();
						break;
				
			}
		}while(sec != 2);
	}

}