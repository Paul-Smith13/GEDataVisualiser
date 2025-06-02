
public class TestClass {
	/*
    Create a function that returns the sum of the two lowest positive numbers given an array of minimum
    4 positive integers.

    For example, when an array is passed like [19, 5, 42, 2, 77], the output should be 7.

*/

	public static int sumTwoSmallestNumbers(int[] nums) {
		
		//Assumption: low1 < low2
		
		int low1 = nums[0];
		int low2 = nums[1];
		
		int numsSorted[] = new int[nums.length];
		for (int i : nums) {
			if (i < low1) {
				low1 = i;
			}
			
		}
		for (int i : nums) {
			if (i < low2 && i > low1) {
				low2 = i;
			}
		}
				
		int result = low1 + low2;	
			
		}
		return result;
	}
	public static void main(String[] args) {
		//Steps:
		int[] nums = {19, 5, 42, 2, 77};
		sumTwoSmallestNumbers(nums);
		
		
	}

}
