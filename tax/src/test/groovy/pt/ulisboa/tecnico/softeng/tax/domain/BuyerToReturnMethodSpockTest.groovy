package pt.ulisboa.tecnico.softeng.tax.domain

import org.joda.time.LocalDate
import pt.ulisboa.tecnico.softeng.tax.exception.TaxException
import spock.lang.Unroll

class BuyerToReturnMethodSpockTest extends SpockRollbackTestAbstractClass {
	def SELLER_NIF = '123456789'
	def BUYER_NIF = '987654321'
	def FOOD = 'FOOD'
	def TAX = 10
	def date = new LocalDate(2018,02,13)
	def seller
	def buyer
	def itemType

	@Override
	def populate4Test() {
		def irs = IRS.getIRSInstance()

		seller = new Seller(irs,SELLER_NIF,'José Vendido','Somewhere')
		buyer = new Buyer(irs,BUYER_NIF,'Manuel Comprado','Anywhere')
		itemType = new ItemType(irs, FOOD, TAX)
	}

	@Unroll('testing success: #year, #val')
	def 'success'() {
		given:
		new Invoice(100, date, itemType, seller, buyer)
		new Invoice(100, date, itemType, seller, buyer)
		new Invoice(50, date, itemType, seller, buyer)

		when:
		def value = buyer.taxReturn(year)

		then:
		val == value

		where:
		year | val
		2018 | 1.25
		2017 | 0.00f
	}


	def 'no invoices'() {
		when:
		def value = buyer.taxReturn(2018)

		then:
		0.00f == value
	}

	def 'before 1970'() {
		when:
		new Invoice(100, new LocalDate(1969,02,13), itemType, seller, buyer)

		then:
		thrown(TaxException)
	}

	def 'equal 1970'() {
		given:
		new Invoice(100,new LocalDate(1970,02,13), itemType, seller, buyer)

		when:
		def value = buyer.taxReturn(1970)

		then:
		0.5 == value
	}

	def 'ignore cancelled'() {
		given:
		new Invoice(100, date, itemType, seller, buyer)
		def invoice = new Invoice(100, date, itemType, seller, buyer)
		new Invoice(50, date, itemType, seller, buyer)

		invoice.cancel()

		when:
		def value = buyer.taxReturn(2018)

		then:
		0.75 == value
	}

}
