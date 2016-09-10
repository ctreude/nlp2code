package ca.mcgill.cs.swevo.taskextractor.model;


import java.util.HashMap;
import java.util.Map;

/**
 * Dictionary that maps code elements to their mask (ce[0-9]+).
 * 
 * @author ctreude
 * 
 */
public final class CodeElementDictionary
{
	private static Map<String, String> aCodeElementsToMask = new HashMap<String, String>();
	private static Map<String, String> aMaskToCodeElements = new HashMap<String, String>();

	private CodeElementDictionary()
	{
	}

	/**
	 * @param pCodeElement
	 *            code element
	 * @return whether dictionary contains code element
	 */
	public static boolean containsCodeElement(String pCodeElement)
	{
		return aCodeElementsToMask.containsKey(pCodeElement);
	}

	/**
	 * @param pMask
	 *            mask
	 * @return code element for mask
	 */
	public static String getCodeElementForMask(String pMask)
	{
		if (aMaskToCodeElements.get(pMask) == null)
		{
			return "";
		}
		else
		{
			return aMaskToCodeElements.get(pMask);
		}
	}

	/**
	 * @return code elements to mask map (only for serialization)
	 */
	public static Map<String, String> getCodeElementsToMask()
	{
		return aCodeElementsToMask;
	}

	/**
	 * @param pCodeElement
	 *            code element
	 * @return mask for code element
	 */
	public static String getMaskForCodeElement(String pCodeElement)
	{
		return aCodeElementsToMask.get(pCodeElement);
	}

	/**
	 * @return mask to code element map (only for serialization)
	 */
	public static Map<String, String> getMaskToCodeElements()
	{
		return aMaskToCodeElements;
	}

	/**
	 * @param pCodeElement
	 *            code element
	 * @param pMask
	 *            mask
	 */
	public static void putCodeElementsToMask(String pCodeElement, String pMask)
	{
		aCodeElementsToMask.put(pCodeElement, pMask);
		aMaskToCodeElements.put(pMask, pCodeElement);
	}

	/**
	 * @param pCodeElementsToMask
	 *            code elements to mask map (only for serialization)
	 */
	public static void setCodeElementsToMask(Map<String, String> pCodeElementsToMask)
	{
		aCodeElementsToMask = pCodeElementsToMask;
	}

	/**
	 * @param pMaskToCodeElements
	 *            mask to code element map (only for serialization)
	 */
	public static void setMaskToCodeElements(Map<String, String> pMaskToCodeElements)
	{
		aMaskToCodeElements = pMaskToCodeElements;
	}
}
