package io.pivotal.security.domain;

import io.pivotal.security.credential.CertificateCredentialValue;
import io.pivotal.security.entity.CertificateCredentialData;
import io.pivotal.security.service.Encryption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class CertificateCredentialTest {

  private CertificateCredential subject;
  private CertificateCredentialData certificateCredentialData;

  private UUID canaryUuid;
  private Encryptor encryptor;

  private byte[] encryptedValue;
  private byte[] nonce;

  @Before
  public void setup() {
    encryptor = mock(Encryptor.class);

    encryptedValue = "fake-encrypted-value".getBytes();
    nonce = "fake-nonce".getBytes();
    canaryUuid = UUID.randomUUID();

    final Encryption encryption = new Encryption(canaryUuid, encryptedValue, nonce);
    when(encryptor.encrypt("my-priv"))
        .thenReturn(encryption);
    when(encryptor.decrypt(encryption)).thenReturn("my-priv");

    certificateCredentialData = new CertificateCredentialData("/Foo");
    subject = new CertificateCredential(certificateCredentialData)
        .setEncryptor(encryptor)
        .setCa("my-ca")
        .setCertificate("my-cert")
        .setPrivateKey("my-priv");
  }

  @Test
  public void getCredentialType_returnsTypeCertificate() {
    assertThat(subject.getCredentialType(), equalTo("certificate"));
  }

  @Test
  public void setPrivateKey_setsEncryptedValueAndNonce() {
    subject.setPrivateKey("my-priv");
    assertThat(certificateCredentialData.getEncryptedValue(), notNullValue());
    assertThat(certificateCredentialData.getNonce(), notNullValue());
  }

  @Test
  public void getPrivateKey_decryptsPrivateKey() {
    subject.setPrivateKey("my-priv");
    assertThat(subject.getPrivateKey(), equalTo("my-priv"));
  }

  @Test
  public void setCaName_addASlashToCaName() {
    subject.setCaName("something");
    assertThat(subject.getCaName(), equalTo("/something"));

    subject.setCaName("/something");
    assertThat(subject.getCaName(), equalTo("/something"));

    subject.setCaName("");
    assertThat(subject.getCaName(), equalTo(""));

    subject.setCaName(null);
    assertThat(subject.getCaName(), equalTo(null));
  }

  @Test
  public void CertificateCredential_withMissingCertificateValue_shouldNotError() {
    final CertificateCredentialValue certificateCredentialValue = new CertificateCredentialValue("someCa", "", "my-priv", "/aCaName");
    final CertificateCredential certificateCredential = new CertificateCredential(certificateCredentialValue, encryptor);

    assertThat(certificateCredential.getCa(), equalTo("someCa"));
    assertThat(certificateCredential.getCertificate(), equalTo(""));
    assertThat(certificateCredential.getPrivateKey(), equalTo("my-priv"));
    assertThat(certificateCredential.getCaName(), equalTo("/aCaName"));
  }
}
