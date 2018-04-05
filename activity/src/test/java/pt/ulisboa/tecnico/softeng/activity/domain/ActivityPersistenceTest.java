package pt.ulisboa.tecnico.softeng.activity.domain;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

import static org.junit.Assert.*;

public class ActivityPersistenceTest {
	private static final String ACTIVITY_NAME = "Activity_Name";
	private static final String PROVIDER_NAME = "Wicket";
	private static final String PROVIDER_CODE = "A12345";
	private static final String IBAN = "IBAN";
	private static final String NIF = "NIF";
	private static final String BUYER_IBAN = "IBAN2";
	private static final String BUYER_NIF = "NIF2";
	private static final int CAPACITY = 25;
	private static final double AMOUNT = 30.0;

	private final LocalDate begin = new LocalDate(2017, 04, 01);
	private final LocalDate end = new LocalDate(2017, 04, 15);

	@Test
	public void success() {
		atomicProcess();
		atomicAssert();
	}

	@Atomic(mode = TxMode.WRITE)
	public void atomicProcess() {
		ActivityProvider activityProvider = new ActivityProvider(PROVIDER_CODE, PROVIDER_NAME, NIF, IBAN);

		Activity activity = new Activity(activityProvider, ACTIVITY_NAME, 18, 65, CAPACITY);

		ActivityOffer activityOffer = new ActivityOffer(activity, this.begin, this.end, AMOUNT);

		new Booking(activityProvider, activityOffer, BUYER_NIF, BUYER_IBAN);
	}

	@Atomic(mode = TxMode.READ)
	public void atomicAssert() {
		assertEquals(1, FenixFramework.getDomainRoot().getActivityProviderSet().size());

		List<ActivityProvider> providers = new ArrayList<>(FenixFramework.getDomainRoot().getActivityProviderSet());
		ActivityProvider provider = providers.get(0);

		assertEquals(PROVIDER_CODE, provider.getCode());
		assertEquals(PROVIDER_NAME, provider.getName());
		assertEquals(1, provider.getActivitySet().size());
		assertEquals(NIF , provider.getNif());
		assertEquals(IBAN, provider.getIban());

		List<Activity> activities = new ArrayList<>(provider.getActivitySet());
		Activity activity = activities.get(0);

		assertEquals(ACTIVITY_NAME, activity.getName());
		assertTrue(activity.getCode().startsWith(PROVIDER_CODE));
		assertEquals(18, activity.getMinAge());
		assertEquals(65, activity.getMaxAge());
		assertEquals(CAPACITY, activity.getCapacity());
		assertEquals(1, activity.getActivityOfferSet().size());

		List<ActivityOffer> offers = new ArrayList<>(activity.getActivityOfferSet());
		ActivityOffer offer = offers.get(0);

		assertEquals(this.begin, offer.getBegin());
		assertEquals(this.end, offer.getEnd());
		assertEquals(CAPACITY, offer.getCapacity());
		assertEquals(1, offer.getBookingSet().size());
		assertEquals(AMOUNT, offer.getPrice(), 0);

		List<Booking> bookings = new ArrayList<>(offer.getBookingSet());
		Booking booking = bookings.get(0);

		assertNotNull(booking.getReference());
		assertNull(booking.getCancel());
		assertNull(booking.getCancellationDate());
		assertNull(booking.getPaymentReference());
		assertNull(booking.getInvoiceReference());
		assertFalse(booking.getCancelledInvoice());
		assertNull(booking.getCancelledPaymentReference());
		assertEquals("SPORT", booking.getType());
		assertEquals(BUYER_NIF, booking.getBuyerNif());
		assertEquals(BUYER_IBAN, booking.getIban());
		assertEquals(NIF, booking.getProviderNif());
    }

	@After
	@Atomic(mode = TxMode.WRITE)
	public void tearDown() {
		for (ActivityProvider activityProvider : FenixFramework.getDomainRoot().getActivityProviderSet()) {
			activityProvider.delete();
		}
	}

}