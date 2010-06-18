package fgdo_java.daemons;

import fgdo_java.database.ExpAvgItem;
import fgdo_java.database.Result;
import fgdo_java.database.Workunit;

public abstract class CreditPolicy {

	public static final double max_credit_per_cpu_sec = 500;
	public static final double SECONDS_PER_DAY = 60 * 60 * 24;
	public static final double CREDIT_HALF_LIFE = SECONDS_PER_DAY * 7;
	public static final double M_LN2 = 0.693147180559945309417;

	/**
	 * Update an estimate of "units per day" of something (credit or CPU time)
	 * The estimate is exponentially averaged with a given half-life
	 * (ie, if no new work is done, the average will decline by 50% in this time)
	 * This function can be called either with new work 
	 * or with zero work to decay an existing average.
	 *
	 * NOTE: if you change this, also change update_average in
	 * html/inc/credit.inc
	 */
	public static void updateAverages(ExpAvgItem item, double start_time, double granted_credit) {
		double now = System.currentTimeMillis() / 1000.0;

		double avg_time = item.getExpAvgTime();
		double avg = item.getExpAvgCredit();

		if (avg_time != 0) {
			// If an average R already exists, imagine that the new work was done
			// entirely between avg_time and now.
			// That gives a rate R'.
			// Replace R with a weighted average of R and R',
			// weighted so that we get the right half-life if R' == 0.
			//
			// But this blows up if avg_time == now; you get 0*(1/0)
			// So consider the limit as diff->0,
			// using the first-order Taylor expansion of
			// exp(x)=1+x+O(x^2).
			// So to the lowest order in diff:
			// weight = 1 - diff ln(2) / half_life
			// so one has
			// avg += (1-weight)*(work/diff_days)
			// avg += [diff*ln(2)/half_life] * (work*SECONDS_PER_DAY/diff)
			// notice that diff cancels out, leaving
			// avg += [ln(2)/half_life] * work*SECONDS_PER_DAY

			double diff, diff_days, weight;

			diff = now - avg_time;
			if (diff < 0) diff = 0;

			diff_days = diff/SECONDS_PER_DAY;
			weight = Math.exp(-diff * M_LN2/CREDIT_HALF_LIFE);

			avg *= weight;

			if ((1.0 - weight) > 1.e-6) {
				avg += (1 - weight) * (granted_credit/diff_days);
			} else {
				avg += M_LN2 * granted_credit * SECONDS_PER_DAY/CREDIT_HALF_LIFE;

			}
		} else if (granted_credit != 0) {
			// If first time, average is just granted_credit/duration
			double dd = (now - start_time)/SECONDS_PER_DAY;
			avg = granted_credit/dd;
		}
		avg_time = now;

		item.setExpAvgTime(avg_time);
		item.setExpAvgCredit(avg);

	}

	public abstract boolean assignCredit(Workunit workunit, Result result);
}
