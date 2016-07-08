// Copyright 2016 The Domain Registry Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.pricing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static google.registry.pricing.PricingEngineProxy.getPricesForDomainName;
import static google.registry.util.PreconditionsUtils.checkArgumentNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import google.registry.model.ImmutableObject;
import google.registry.model.domain.fee.EapFee;
import google.registry.model.domain.fee.Fee;
import google.registry.model.pricing.PremiumPricingEngine.DomainPrices;
import google.registry.model.registry.Registry;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;

/**
 * Provides specialized pricing, billing and update logic per TLD.  TODO: consider making these
 * methods of Registry.
 */
public final class TldSpecificLogicEngine {

  private static final String EAP_DESCRIPTION_FORMAT = "Early Access Period, fee expires: %s";

  /**
   * A collection of fees for a specific event.
   */
  public static final class EppCommandOperations extends ImmutableObject {
    private final CurrencyUnit currency;
    private final ImmutableList<Fee> fees;

    EppCommandOperations(CurrencyUnit currency, ImmutableList<Fee> fees) {
      this.currency = checkArgumentNotNull(
          currency, "Currency may not be null in EppCommandOperations.");
      checkArgument(!fees.isEmpty(), "You must specify one or more fees.");
      this.fees = checkArgumentNotNull(fees, "Fees may not be null in EppCommandOperations.");
    }

    /**
     * Returns the total cost of all fees for the event.
     */
    public Money getTotalCost() {
      Money result = Money.of(currency, 0);
      for (Fee fee : fees) {
        result = result.plus(fee.getCost());
      }
      return result;
    }

    /**
     * Returns all costs for the event as a list of fees.
     */
    public ImmutableList<Fee> getFees() {
      return fees;
    }

    /**
     * Returns the currency for all fees in the event.
     */
    public final CurrencyUnit getCurrency() {
      return currency;
    }
  }

  private TldSpecificLogicEngine() {}

  /**
   * Returns a new "create" price for the Pricer.
   */
  public static EppCommandOperations getCreatePrice(
      Registry registry, String domainName, DateTime date, int years) {
    DomainPrices prices = getPricesForDomainName(domainName, date);
    CurrencyUnit currency = registry.getCurrency();
    ImmutableList.Builder<Fee> feeBuilder = new ImmutableList.Builder<>();

    // Add Create cost.
    feeBuilder.add(Fee.create(prices.getCreateCost().multipliedBy(years).getAmount(), "create"));

    // Add EAP Fee.
    EapFee eapFee = registry.getEapFeeFor(date);
    Money eapFeeCost = eapFee.getCost();
    checkState(eapFeeCost.getCurrencyUnit().equals(currency));
    if (!eapFeeCost.getAmount().equals(Money.zero(currency).getAmount())) {
      feeBuilder.add(
          Fee.create(
              eapFeeCost.getAmount(),
              String.format(EAP_DESCRIPTION_FORMAT, eapFee.getPeriod().upperEndpoint())));
    }

    return new EppCommandOperations(currency, feeBuilder.build());
  }

  /**
   * Returns a new renew price for the pricer.
   *
   * <p>domain name, number of years and date must be defined before calling this.
   */
  public static EppCommandOperations getRenewPrice(
      Registry registry, String domainName, DateTime date, int years) {
    DomainPrices prices = getPricesForDomainName(domainName, date);
    return new EppCommandOperations(
        registry.getCurrency(),
        ImmutableList.of(
            Fee.create(prices.getRenewCost().multipliedBy(years).getAmount(), "renew")));
  }

  /**
   * Returns a new restore price (including the renew price) for the pricer.
   *
   * <p>domain name, number of years and date must be defined before calling this.
   */
  public static EppCommandOperations getRestorePrice(
      Registry registry, String domainName, DateTime date, int years) {
    DomainPrices prices = getPricesForDomainName(domainName, date);
    return new EppCommandOperations(
        registry.getCurrency(),
        ImmutableList.of(
            Fee.create(prices.getRenewCost().multipliedBy(years).getAmount(), "renew"),
            Fee.create(registry.getStandardRestoreCost().getAmount(), "restore")));
  }

  /**
   * Returns the fee class for a given domain and date.
   */
  public static Optional<String> getFeeClass(String domainName, DateTime date) {
    return getPricesForDomainName(domainName, date).getFeeClass();
  }

  // TODO(b/29089413): Add support for transfer prices once this is plumbed through the flows.
}