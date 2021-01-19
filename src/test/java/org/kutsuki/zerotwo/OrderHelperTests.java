package org.kutsuki.zerotwo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderHelper;
import org.kutsuki.zerotwo.portfolio.OrderModel;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderHelperTests {
    private OrderHelper helper;

    public OrderHelperTests() {
	this.helper = new OrderHelper();
    }

    @Test
    public void backRatioTest() throws Exception {
	String test1 = "#456 BOT +2 1/3 BACKRATIO NFLX 100 16 OCT 20 585/600 CALL @.05db ISE  Closing half at close to zero to decrease margin.";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(456, -2, "NFLX", LocalDate.of(2020, 10, 16), false, BigDecimal.valueOf(585),
		OptionType.CALL));
	expected.add(new Position(456, 6, "NFLX", LocalDate.of(2020, 10, 16), false, BigDecimal.valueOf(600),
		OptionType.CALL));
	testOrder(actual, expected, "BACKRATIO", "NFLX", BigDecimal.valueOf(.05), "NET_DEBIT");

	String test2 = "#455 NEW SOLD -6 1/2 BACKRATIO QQQ 100 16 OCT 20 285/290 CALL @-.52db CBOE";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(455, 6, "QQQ", LocalDate.of(2020, 10, 16), false, BigDecimal.valueOf(285),
		OptionType.CALL));
	expected.add(new Position(455, -12, "QQQ", LocalDate.of(2020, 10, 16), false, BigDecimal.valueOf(290),
		OptionType.CALL));
	testOrder(actual, expected, "BACKRATIO", "QQQ", BigDecimal.valueOf(-.52), "NET_DEBIT");
    }

    @Test
    public void butterflyTest() throws Exception {
	String test1 = "#462 NEW BOT +1 BUTTERFLY SPX 100 (Weeklys) 28 OCT 20 3480/3490/3540 CALL @-.90cr CBOE";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(462, 1, "SPX", LocalDate.of(2020, 10, 28), false, BigDecimal.valueOf(3480),
		OptionType.CALL));
	expected.add(new Position(462, -2, "SPX", LocalDate.of(2020, 10, 28), false, BigDecimal.valueOf(3490),
		OptionType.CALL));
	expected.add(new Position(462, 1, "SPX", LocalDate.of(2020, 10, 28), false, BigDecimal.valueOf(3540),
		OptionType.CALL));
	testOrder(actual, expected, "BUTTERFLY", "SPX", BigDecimal.valueOf(-.90), "NET_CREDIT");

	String test2 = "#431 SOLD -2 BUTTERFLY TSLA 100 (Weeklys) 4 SEP 20 580/600/680 CALL @-.10db CBOE";
	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(431, -2, "TSLA", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(580),
		OptionType.CALL));
	expected.add(new Position(431, 4, "TSLA", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(600),
		OptionType.CALL));
	expected.add(new Position(431, -2, "TSLA", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(680),
		OptionType.CALL));
	testOrder(actual, expected, "BUTTERFLY", "TSLA", BigDecimal.valueOf(-.10), "NET_DEBIT");
    }

    @Test
    public void condorTest() throws Exception {
	String test1 = "#441 BOT +2 CONDOR SPX 18 SEP 20 3470/3475/3490/3555 CALL @-.95cr CBOE (2 of potential 4--above .80cr is good)";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(441, 2, "SPX", LocalDate.of(2020, 9, 18), false, BigDecimal.valueOf(3470),
		OptionType.CALL));
	expected.add(new Position(441, -2, "SPX", LocalDate.of(2020, 9, 18), false, BigDecimal.valueOf(3475),
		OptionType.CALL));
	expected.add(new Position(441, -2, "SPX", LocalDate.of(2020, 9, 18), false, BigDecimal.valueOf(3490),
		OptionType.CALL));
	expected.add(new Position(441, 2, "SPX", LocalDate.of(2020, 9, 18), false, BigDecimal.valueOf(3555),
		OptionType.CALL));
	testOrder(actual, expected, "CONDOR", "SPX", BigDecimal.valueOf(-.95), "NET_CREDIT");

	String test2 = "#400 SOLD -1 CONDOR AAPL 10 JUL 20 365/370/372.5/377.5 CALL @1.28 CBOE  Closing out here.  We only have one unit officially and AAPL is breaking to new ATH.";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(400, -1, "AAPL", LocalDate.of(2020, 7, 10), false, BigDecimal.valueOf(365),
		OptionType.CALL));
	expected.add(new Position(400, 1, "AAPL", LocalDate.of(2020, 7, 10), false, BigDecimal.valueOf(370),
		OptionType.CALL));
	expected.add(new Position(400, 1, "AAPL", LocalDate.of(2020, 7, 10), false, BigDecimal.valueOf(372.5),
		OptionType.CALL));
	expected.add(new Position(400, -1, "AAPL", LocalDate.of(2020, 7, 10), false, BigDecimal.valueOf(377.5),
		OptionType.CALL));
	testOrder(actual, expected, "CONDOR", "AAPL", BigDecimal.valueOf(1.28), "NET_CREDIT");
    }

    @Test
    public void diagonalTest() throws Exception {
	String test1 = "#436 SOLD -2 DIAGONAL UVXY 100 16 OCT 20/25 SEP 20 17/19.5 PUT @-.25db PHLX";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(436, -2, "UVXY", LocalDate.of(2020, 10, 16), false, BigDecimal.valueOf(17),
		OptionType.PUT));
	expected.add(new Position(436, 2, "UVXY", LocalDate.of(2020, 9, 25), false, BigDecimal.valueOf(19.5),
		OptionType.PUT));
	testOrder(actual, expected, "DIAGONAL", "UVXY", BigDecimal.valueOf(-.25), "NET_DEBIT");

	String test2 = "#414 SOLD -2 DIAGONAL GLD 100 (Weeklys) 11 SEP 20/21 AUG 20 175/180 PUT @-.42db CBOE";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(414, -2, "GLD", LocalDate.of(2020, 9, 11), false, BigDecimal.valueOf(175),
		OptionType.PUT));
	expected.add(
		new Position(414, 2, "GLD", LocalDate.of(2020, 8, 21), false, BigDecimal.valueOf(180), OptionType.PUT));
	testOrder(actual, expected, "DIAGONAL", "GLD", BigDecimal.valueOf(-.42), "NET_DEBIT");
    }

    @Test
    public void ironCondorTest() throws Exception {
	String test1 = "#467 NEW SOLD -1 IRON CONDOR AAPL 100 (Weeklys) 30 OCT 20 110/111/110/109 CALL/PUT @.80 MIAX  Iron butterfly to illustrate concept with tiny size and risk.";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(467, -1, "AAPL", LocalDate.of(2020, 10, 30), false, BigDecimal.valueOf(110),
		OptionType.CALL));
	expected.add(new Position(467, 1, "AAPL", LocalDate.of(2020, 10, 30), false, BigDecimal.valueOf(111),
		OptionType.CALL));
	expected.add(new Position(467, -1, "AAPL", LocalDate.of(2020, 10, 30), false, BigDecimal.valueOf(110),
		OptionType.PUT));
	expected.add(new Position(467, 1, "AAPL", LocalDate.of(2020, 10, 30), false, BigDecimal.valueOf(109),
		OptionType.PUT));
	testOrder(actual, expected, "IRON CONDOR", "AAPL", BigDecimal.valueOf(.80), "NET_CREDIT");

	String test2 = "#418 NEW SOLD -2 IRON CONDOR FB 7 AUG 20 250/252.5/250/247.5 CALL/PUT @2.09 CBOE  iron butterfly for staying flat into end of week.  good r/r.";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(
		new Position(418, -2, "FB", LocalDate.of(2020, 8, 7), false, BigDecimal.valueOf(250), OptionType.CALL));
	expected.add(new Position(418, 2, "FB", LocalDate.of(2020, 8, 7), false, BigDecimal.valueOf(252.5),
		OptionType.CALL));
	expected.add(
		new Position(418, -2, "FB", LocalDate.of(2020, 8, 7), false, BigDecimal.valueOf(250), OptionType.PUT));
	expected.add(
		new Position(418, 2, "FB", LocalDate.of(2020, 8, 7), false, BigDecimal.valueOf(247.5), OptionType.PUT));
	testOrder(actual, expected, "IRON CONDOR", "FB", BigDecimal.valueOf(2.09), "NET_CREDIT");
    }

    @Test
    public void singleTest() throws Exception {
	String test1 = "#414 BOT +2 GLD 100 (Weeklys) 9 OCT 20 184 CALL @.32 BATS  Closing these at b/e due to the current news cycle to avoid potential risk--GLD was up $4 overnight when that news hit.  Will reassess later.";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(414, 2, "GLD", LocalDate.of(2020, 10, 9), false, BigDecimal.valueOf(184),
		OptionType.CALL));
	testOrder(actual, expected, "CALL", "GLD", BigDecimal.valueOf(.32), "LIMIT");

	String test2 = "#414 SOLD -4 GLD 6 NOV 20 172 PUT @.38 BATS We are now full size on both sides for 11.6.  Will assess the credits on afternoon of 11.3 just before election.";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(414, -4, "GLD", LocalDate.of(2020, 11, 6), false, BigDecimal.valueOf(172),
		OptionType.PUT));
	testOrder(actual, expected, "PUT", "GLD", BigDecimal.valueOf(.38), "LIMIT");
    }

    @Test
    public void unbalancedButterflyTest() throws Exception {
	String test1 = "#424 BOT +2 1/3/2 ~BUTTERFLY SPX 100 (Weeklys) 21 AUG 20 3405/3420/3450 CALL @.80 CBOE";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(424, 2, "SPX", LocalDate.of(2020, 8, 21), false, BigDecimal.valueOf(3405),
		OptionType.CALL));
	expected.add(new Position(424, -6, "SPX", LocalDate.of(2020, 8, 21), false, BigDecimal.valueOf(3420),
		OptionType.CALL));
	expected.add(new Position(424, 4, "SPX", LocalDate.of(2020, 8, 21), false, BigDecimal.valueOf(3450),
		OptionType.CALL));
	testOrder(actual, expected, "~BUTTERFLY", "SPX", BigDecimal.valueOf(.80), "NET_DEBIT");

	String test2 = "#423 SOLD -1 1/3/2 ~BUTTERFLY SPX 17 AUG 20 3385/3400/3415 CALL @ 3.00  S&P holding in middle of range close to open.  Close incrementally if you have more spreads.";
	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(423, -1, "SPX", LocalDate.of(2020, 8, 17), false, BigDecimal.valueOf(3385),
		OptionType.CALL));
	expected.add(new Position(423, 3, "SPX", LocalDate.of(2020, 8, 17), false, BigDecimal.valueOf(3400),
		OptionType.CALL));
	expected.add(new Position(423, -2, "SPX", LocalDate.of(2020, 8, 17), false, BigDecimal.valueOf(3415),
		OptionType.CALL));
	testOrder(actual, expected, "~BUTTERFLY", "SPX", BigDecimal.valueOf(3.00), "NET_CREDIT");
    }

    @Test
    public void verticalTest() throws Exception {
	String test1 = "#458 BOT +1 VERTICAL SPX 21 OCT 20 3375/3365 PUT @1.45 CBOE tightens it up to only $5 of downside risk.  Looks more bearish now.";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(458, 1, "SPX", LocalDate.of(2020, 10, 21), false, BigDecimal.valueOf(3375),
		OptionType.PUT));
	expected.add(new Position(458, -1, "SPX", LocalDate.of(2020, 10, 21), false, BigDecimal.valueOf(3365),
		OptionType.PUT));
	testOrder(actual, expected, "VERTICAL", "SPX", BigDecimal.valueOf(1.45), "NET_DEBIT");

	String test2 = "#426 SOLD -2 VERTICAL BABA 100 (Weeklys) 4 SEP 20 280/285 CALL @2.50 PHLX  Closing this early for small gain, short calls remain open.  Don't like market long here.";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(426, -2, "BABA", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(280),
		OptionType.CALL));
	expected.add(new Position(426, 2, "BABA", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(285),
		OptionType.CALL));
	testOrder(actual, expected, "VERTICAL", "BABA", BigDecimal.valueOf(2.50), "NET_CREDIT");
    }

    @Test
    public void twoTest() throws Exception {
	String test1 = "#527 NEW WORKING SELL -2 RIOT 22 JAN 21 16 PUT @.30 LMT GTC & BUY +1 BUTTERFLY RIOT 22 JAN 21 25/30/35 CALL @.65 LMT GTC 1 and 2 units to start, let's see if any of this fills before committing further.";

	List<OrderModel> actual = helper.createOrders(test1);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(527, -2, "RIOT", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(16),
		OptionType.PUT));
	List<Position> expected2 = new ArrayList<Position>();
	expected2.add(new Position(527, 1, "RIOT", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(25),
		OptionType.CALL));
	expected2.add(new Position(527, -2, "RIOT", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(30),
		OptionType.CALL));
	expected2.add(new Position(527, 1, "RIOT", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(35),
		OptionType.CALL));
	testOrder(actual.get(0), expected, "PUT", "RIOT", BigDecimal.valueOf(.30), "LIMIT", true, false, true, 0);
	testOrder(actual.get(1), expected2, "BUTTERFLY", "RIOT", BigDecimal.valueOf(.65), "NET_DEBIT", true, false,
		true, 0);

	String test2 = "#414 SOLD -4 GLD 6 NOV 20 172 PUT @.38 BATS & SOLD -2 GLD 13 NOV 20 186.5 CALL @.39 BATS  We are now full size on both sides for 11.6.  Will assess the credits on afternoon of 11.3 just before election.";

	actual = helper.createOrders(test2);
	expected = new ArrayList<Position>();
	expected.add(new Position(414, -4, "GLD", LocalDate.of(2020, 11, 6), false, BigDecimal.valueOf(172),
		OptionType.PUT));
	expected2 = new ArrayList<Position>();
	expected2.add(new Position(414, -2, "GLD", LocalDate.of(2020, 11, 13), false, BigDecimal.valueOf(186.5),
		OptionType.CALL));
	testOrder(actual.get(0), expected, "PUT", "GLD", BigDecimal.valueOf(.38), "LIMIT");
	testOrder(actual.get(1), expected2, "CALL", "GLD", BigDecimal.valueOf(.39), "LIMIT");
    }

    @Test
    public void stopTest() throws Exception {
	String test1 = "#519 WORKING SELL -1 VERTICAL SPY 100 (Weeklys) 8 JAN 21 376/379 CALL STP 2.00 MARK Stop limit order on other unit. Not willing to gamble for that extra dollar into tomorrow (50/50 bet)";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(519, -1, "SPY", LocalDate.of(2021, 1, 8), false, BigDecimal.valueOf(376),
		OptionType.CALL));
	expected.add(
		new Position(519, 1, "SPY", LocalDate.of(2021, 1, 8), false, BigDecimal.valueOf(379), OptionType.CALL));
	testOrder(actual, expected, "VERTICAL", "SPY", BigDecimal.valueOf(2.00), "NET_CREDIT", false, true, true, 0);

	String test2 = "#520 WORKING SELL -5 TSLA 100 15 JAN 21 1020 CALL STP .65 Stop limit order on the 1020 calls that turns in to bwb if filled and clears debit on trade.";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(520, -5, "TSLA", LocalDate.of(2021, 1, 15), false, BigDecimal.valueOf(1020),
		OptionType.CALL));
	testOrder(actual, expected, "CALL", "TSLA", BigDecimal.valueOf(.65), "LIMIT", false, true, true, 0);
    }

    @Test
    public void conditionTest() throws Exception {
	String test1 = "#465 WORKING BUY +1 SPX 30 OCT 20 3415 CALL STP WHEN SPX MARK AT OR ABOVE 3342.00";

	OrderModel actual = helper.createOrders(test1).get(0);
	List<Position> expected = new ArrayList<Position>();
	expected.add(new Position(465, 1, "SPX", LocalDate.of(2020, 10, 30), false, BigDecimal.valueOf(3415),
		OptionType.CALL));
	testOrder(actual, expected, "CALL", "SPX", BigDecimal.valueOf(3342.00), "LIMIT", false, true, true, 1);

	String test2 = "#432 NEW WORKING SELL -1 BUTTERFLY NFLX 4 SEP 20 550/570/590 CALL MKT WHEN NFLX MARK AT OR BELOW 550.00";

	actual = helper.createOrders(test2).get(0);
	expected = new ArrayList<Position>();
	expected.add(new Position(432, -1, "NFLX", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(550),
		OptionType.CALL));
	expected.add(new Position(432, 2, "NFLX", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(570),
		OptionType.CALL));
	expected.add(new Position(432, -1, "NFLX", LocalDate.of(2020, 9, 4), false, BigDecimal.valueOf(590),
		OptionType.CALL));
	testOrder(actual, expected, "BUTTERFLY", "NFLX", BigDecimal.valueOf(550.00), "MARKET", false, false, true, -1);
    }

    private void testOrder(OrderModel actual, List<Position> expected, String spread, String symbol, BigDecimal price,
	    String orderType) {
	testOrder(actual, expected, spread, symbol, price, orderType, false, false, false, 0);
    }

    private void testOrder(OrderModel actual, List<Position> expected, String spread, String symbol, BigDecimal price,
	    String orderType, boolean gtc, boolean stop, boolean working, int condition) {
	Assertions.assertEquals(spread, actual.getSpread(), "Wrong Spread");
	Assertions.assertEquals(symbol, actual.getSymbol(), "Wrong Order Symbol");
	Assertions.assertTrue(price.compareTo(actual.getPrice()) == 0, "Wrong Order Price");
	Assertions.assertEquals(orderType, actual.getOrderType(), "Wrong Order Type");
	Assertions.assertEquals(gtc, actual.isGTC(), "Wrong GTC");
	Assertions.assertEquals(stop, actual.isStop(), "Wrong Stop");
	Assertions.assertEquals(working, actual.isWorking(), "Wrong Working");
	Assertions.assertEquals(condition, actual.getCondition(), "Wrong Condition");

	Assertions.assertEquals(expected.size(), actual.getPositionList().size(), "Wrong Position List Size");
	for (int i = 0; i < expected.size(); i++) {
	    Position expectedP = expected.get(i);
	    Position actualP = actual.getPositionList().get(i);
	    Assertions.assertEquals(expectedP.getTradeId(), actualP.getTradeId(), "Wrong Trade ID");
	    Assertions.assertEquals(expectedP.getQuantity(), actualP.getQuantity(), "Wrong Quantity");
	    Assertions.assertEquals(expectedP.getSymbol(), actualP.getSymbol(), "Wrong Position Symbol");
	    Assertions.assertEquals(expectedP.getExpiry(), actualP.getExpiry(), "Wrong Expiry");
	    Assertions.assertEquals(expectedP.isAM(), actualP.isAM(), "Wrong AM");
	    Assertions.assertEquals(expectedP.getStrike(), actualP.getStrike(), "Wrong Strike");
	    Assertions.assertEquals(expectedP.getType(), actualP.getType(), "Wrong Type");
	}
    }
}
