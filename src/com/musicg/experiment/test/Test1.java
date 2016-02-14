/*
 *
 */
package com.musicg.experiment.test;

import com.musicg.graphic.GraphicRender;
import com.musicg.math.rank.ArrayRankDouble;
import com.musicg.math.statistics.StandardDeviation;
import com.musicg.pitch.PitchHandler;
import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

public class Test1 {

	public static void main(final String[] args) {

		final String filename = "audio_work/lala.wav";

		// create a wave object
		final Wave wave = new Wave(filename);

		// TimeDomainRepresentations
		final int fftSampleSize = 1024;
		final int overlapFactor = 1;
		final Spectrogram spectrogram = new Spectrogram(wave, fftSampleSize, overlapFactor);

		final int fps = spectrogram.getFramesPerSecond();
		final double unitFrequency = spectrogram.getUnitFrequency();

		// set boundary
		final int highPass = 100;
		final int lowerBoundary = (int) (highPass / unitFrequency);
		final int lowPass = 4000;
		final int upperBoundary = (int) (lowPass / unitFrequency);
		// end set boundary

		final double[][] spectrogramData = spectrogram.getNormalizedSpectrogramData();
		final double[][] absoluteSpectrogramData = spectrogram.getAbsoluteSpectrogramData();
		final double[][] boundedSpectrogramData = new double[spectrogramData.length][];

		//SpectralCentroid sc=new SpectralCentroid();
		final StandardDeviation sd = new StandardDeviation();
		final ArrayRankDouble arrayRankDouble = new ArrayRankDouble();

		// zrc
		final short[] amps = wave.getSampleAmplitudes();
		final int numFrame = amps.length / 1024;
		final double[] zcrs = new double[numFrame];

		for (int i = 0; i < numFrame; i++) {
			final short[] temp = new short[1024];
			System.arraycopy(amps, i * 1024, temp, 0, temp.length);

			int numZC = 0;
			final int size = temp.length;

			for (int j = 0; j < size - 1; j++) {
				if ((temp[j] >= 0 && temp[j + 1] < 0) || (temp[j] < 0 && temp[j + 1] >= 0)) {
					numZC++;
				}
			}

			zcrs[i] = numZC;
		}

		// end zcr

		for (int i = 0; i < spectrogramData.length; i++) {
			final double[] temp = new double[upperBoundary - lowerBoundary + 1];
			System.arraycopy(spectrogramData[i], lowerBoundary, temp, 0, temp.length);

			final int maxIndex = arrayRankDouble.getMaxValueIndex(temp);
			//sc.setValues(temp);
			sd.setValues(temp);
			final double sdValue = sd.evaluate();

			System.out.println(i + " " + (double) i / fps + "s\t" + maxIndex + "\t" + sdValue + "\t" + zcrs[i]);
			boundedSpectrogramData[i] = temp;
		}

		// Graphic render
		final GraphicRender render = new GraphicRender();
		render.setHorizontalMarker(61);
		render.setVerticalMarker(200);
		render.renderSpectrogramData(boundedSpectrogramData, filename + ".jpg");

		final PitchHandler ph = new PitchHandler();

		for (int frame = 0; frame < absoluteSpectrogramData.length; frame++) {

			System.out.print("frame " + frame + ": ");

			final double[] temp = new double[upperBoundary - lowerBoundary + 1];
			sd.setValues(temp);
			final double sdValue = sd.evaluate();
			final double passSd = 0.1;

			if (sdValue < passSd) {
				System.arraycopy(spectrogramData[frame], lowerBoundary, temp, 0, temp.length);
				final double maxFrequency = arrayRankDouble.getMaxValueIndex(temp) * unitFrequency;

				final double passFrequency = 400;
				final int numRobust = 2;

				final double[] robustFrequencies = new double[numRobust];
				final double nthValue = arrayRankDouble.getNthOrderedValue(temp, numRobust, false);
				int count = 0;
				for (int b = lowerBoundary; b <= upperBoundary; b++) {
					if (spectrogramData[frame][b] >= nthValue) {
						robustFrequencies[count++] = b * unitFrequency;
						if (count >= numRobust) {
							break;
						}
					}
				}

				final double passIntensity = 1000;
				double intensity = 0;
				for (int i = 0; i < absoluteSpectrogramData[frame].length; i++) {
					intensity += absoluteSpectrogramData[frame][i];
				}
				intensity /= absoluteSpectrogramData[frame].length;
				System.out.print(" intensity: " + intensity + " pitch: " + maxFrequency);
				if (intensity > passIntensity && maxFrequency > passFrequency) {
					final double p = ph.getHarmonicProbability(robustFrequencies);
					System.out.print(" P: " + p);
				}
			}
			System.out.print(" zcr:" + zcrs[frame]);
			System.out.println();
		}
	}
}