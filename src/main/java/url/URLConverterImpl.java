package url;

/**
 * Created by Dominic Hauton on 05/09/2016.
 *
 * Basic implementation with no conversion
 */
public class URLConverterImpl implements URLConverter {

  /**
   * Identity function
   *
   * @throws URLInvalidException Does not throw
   */
  @Override
  public String convertLink(String inputLink) throws URLInvalidException {
    return inputLink;
  }
}
