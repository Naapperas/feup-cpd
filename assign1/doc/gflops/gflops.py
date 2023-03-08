import os

files = ["c_128.txt", "c_256.txt", "c_512.txt", "c_line_mult.txt", "c_normal_mult.txt"]
output_file = "out/summary.md"

with open(output_file, "w") as f_out:

    for filename in files:

        with open(filename, "r") as f_in:

            lines = f_in.readlines()
            gflops = []
            sizes = []
            times = []

            for line in lines:
                parsed = line.split()
                size = int(parsed[0])
                time = float(parsed[2])
                dcm1 = int(parsed[4])
                dcm2 = int(parsed[6])

                gflop = 2 * (size**3) / time

                gflops.append(gflop)
                sizes.append(size)
                times.append(time)

            # Extract the base filename without the extension
            base_filename = os.path.splitext(filename)[0]

            # Write the table header and data to the output file
            f_out.write(f"## {base_filename.capitalize()} Results\n")
            f_out.write("| Matrix size | Execution time(s) | GFLOPS | L1 Data Cache Misses | L2 Data Cache Misses |\n")
            f_out.write("| ----------- | ----------------- | ------ | -------------------- | -------------------- |\n")
            for i in range(len(gflops)):
                f_out.write(f"| {sizes[i]} | {times[i]:.3f} | {gflops[i]:.3f} | {dcm1} | {dcm2} |\n")

        f_out.write("\n")
