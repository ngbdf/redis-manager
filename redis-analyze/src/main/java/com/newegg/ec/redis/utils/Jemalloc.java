package com.newegg.ec.redis.utils;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年4月3日 下午4:12:09
 * @version:1.0
 * @description:
 */
public class Jemalloc {

	private static long[] array16;

	private static long[] array192;

	private static long[] array768;

	private static long[] array4096;

	private static long[] array4194304;

	static {
		array16 = range(16, 128 + 1, 16);

		array192 = range(192, 512 + 1, 64);

		array768 = range(768, 4096 + 1, 256);

		array4096 = range(4096, 4194304 + 1, 4096);

		array4194304 = range(4194304, 536870912 + 1, 4194304);

	}

	/**
	 * 根据Jemalloc 估算分配内存大小 Small: All 2^n-aligned allocations of size 2^n will incur
	 * no additional overhead, due to how small allocations are aligned and packed.
	 * Small: [8], [16, 32, 48, ..., 128], [192, 256, 320, ..., 512], [768, 1024,
	 * 1280, ..., 3840] Large: The worst case size is half the chunk size, in which
	 * case only one allocation per chunk can be allocated. If the remaining
	 * (nearly) half of the chunk isn't otherwise useful for smaller allocations,
	 * the overhead will essentially be 50%. However, assuming you use a diverse
	 * mixture of size classes, the actual overhead shouldn't be a significant issue
	 * in practice. Large: [4 KiB, 8 KiB, 12 KiB, ..., 4072 KiB] Huge: Extra virtual
	 * memory is mapped, then the excess is trimmed and unmapped. This can leave
	 * virtual memory holes, but it incurs no physical memory overhead. Earlier
	 * versions of jemalloc heuristically attempted to optimistically map chunks
	 * without excess that would need to be trimmed, but it didn't save much system
	 * call overhead in practice. Huge: [4 MiB, 8 MiB, 12 MiB, ..., 512 MiB]
	 * 
	 * @param size
	 * @return
	 */
	public static long assign(long size) {
		if (size <= 4096) {
			// Small
			if (is_power2(size)) {
				return size;
			} else if (size < 128) {
				return min_ge(array16, size);
			} else if (size < 512) {
				return min_ge(array192, size);
			} else {
				return min_ge(array768, size);
			}
		} else if (size < 4194304) {
			// Large
			return min_ge(array4096, size);
		} else {
			// Huge
			return min_ge(array4194304, size);
		}
	}

	/**
	 * 创建一个long数组
	 * 
	 * @param start
	 * @param stop
	 * @param step
	 * @return
	 */
	public static long[] range(int start, int stop, int step) {
		int size = (stop - 1 - start) / step + 1;
		long[] array = new long[size];
		int index = 0;
		for (int i = start; i < stop; i = i + step) {
			array[index] = i;
			index++;
		}
		return array;
	}

	public static long min_ge(long[] srcArray, long key) {
		int index = binarySearch(srcArray, key);
		return srcArray[index];
	}

	// 二分查找最小值，即最接近要查找的值，但是要大于该值
	public static int binarySearch(long srcArray[], long key) {
		int mid = (0+srcArray.length-1) / 2;
		if (key == srcArray[mid]) {
			return mid;
		}

		if (key > srcArray[mid] && key <= srcArray[mid + 1]) {
			return mid + 1;
		}

		int start = 0;
		int end = srcArray.length - 1;
		while (start <= end) {
			mid = (end - start) / 2 + start;
			if (key == srcArray[mid]) {
				return mid;
			}
			if (key > srcArray[mid] && key <= srcArray[mid + 1]) {
				return mid + 1;
			}
			if (key < srcArray[mid]) {
				end = mid - 1;
			}
			if (key > srcArray[mid]) {
				start = mid + 1;
			}
		}
		return 0;
	}

	public static boolean is_power2(long size) {
		if (size == 0) {
			return false;
		}

		if ((size & (size - 1)) == 0) {
			return true;
		}

		return false;
	}

	public static void console(long[] array) {
		for (long v : array) {
			System.out.println(v);
		}
	}

	public static void main(String[] args) {
		console(Jemalloc.array16);
		console(Jemalloc.array192);
		console(Jemalloc.array768);
		console(Jemalloc.array4096);
		console(Jemalloc.array4194304);
	}

}
