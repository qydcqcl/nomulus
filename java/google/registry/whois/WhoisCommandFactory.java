// Copyright 2016 The Nomulus Authors. All Rights Reserved.
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

package google.registry.whois;

import com.google.common.net.InternetDomainName;
import google.registry.config.RegistryConfig.ConfigModule;
import java.net.InetAddress;
import javax.annotation.Nullable;

/**
 * A class used to configure WHOIS commands.
 *
 * <p>To add custom commands, extend this class, then configure it in
 * {@link ConfigModule#provideWhoisCommandFactoryClass()}.
 */
public class WhoisCommandFactory {

  /** Returns a new {@link WhoisCommand} to perform a domain lookup on the specified domain name. */
  public final WhoisCommand domainLookup(InternetDomainName domainName) {
    return domainLookup(domainName, null);
  }

  /**
   * Returns a new {@link WhoisCommand} to perform a domain lookup on the specified domain name in
   * the specified TLD.
   */
  public WhoisCommand domainLookup(
      InternetDomainName domainName, @Nullable InternetDomainName tld) {
    return new DomainLookupCommand(domainName, tld);
  }

  /**
   * Returns a new {@link WhoisCommand} to perform a nameserver lookup on the specified IP address.
   */
  public WhoisCommand nameserverLookupByIp(InetAddress inetAddress) {
    return new NameserverLookupByIpCommand(inetAddress);
  }

  /**
   * Returns a new {@link WhoisCommand} to perform a nameserver lookup on the specified host name.
   */
  public final WhoisCommand nameserverLookupByHost(InternetDomainName hostName) {
    return nameserverLookupByHost(hostName, null);
  }

  /**
   * Returns a new {@link WhoisCommand} to perform a nameserver lookup on the specified host name in
   * the specified TLD.
   */
  public WhoisCommand nameserverLookupByHost(
      InternetDomainName hostName, @Nullable InternetDomainName tld) {
    return new NameserverLookupByHostCommand(hostName, tld);
  }

  /**
   * Returns a new {@link WhoisCommand} to perform a registrar lookup on the specified registrar
   * name.
   */
  public WhoisCommand registrarLookup(String registrar) {
    return new RegistrarLookupCommand(registrar);
  }
}