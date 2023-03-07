#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

void setupMatrices(int mx_size, double **pha, double **phb, double **phc) {
    int i,j;

    *pha = new double[mx_size * mx_size];
    *phb = new double[mx_size * mx_size];
    *phc = new double[mx_size * mx_size];

    for (i = 0; i < mx_size; ++i)
        for (j = 0; j < mx_size; ++j)
            (*pha)[i * mx_size + j] = (double)1.0;

    for (i = 0; i < mx_size; ++i)
        for (j = 0; j < mx_size; ++j)
            (*phb)[i * mx_size + j] = (double)(i+1);

    for (i = 0; i < mx_size; ++i)
        for (j = 0; j < mx_size; ++j)
            (*phc)[i * mx_size + j] = (double)(i+1);
}

void cleanupMatrices(double *pha, double *phb, double *phc) {
    delete pha;
    delete phb;
    delete phc;
}

void OnMult(int mx_size)
{

    SYSTEMTIME Time1, Time2;

    char st[100];
    int i, j, k;

    double *pha, *phb, *phc;

    // Initialize matrices
    setupMatrices(mx_size, &pha, &phb, &phc);

    Time1 = clock();

    for (i = 0; i < mx_size; ++i)
        for (j = 0; j < mx_size; ++j)
            for (k = 0; k < mx_size; ++k)
                phc[i * mx_size + j] += pha[i * mx_size + k] * phb[k * mx_size + j];

    Time2 = clock();
    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for (i = 0; i < 1; i++)
        for (j = 0; j < min(10, mx_size); j++)
            cout << phc[j] << " ";
    cout << endl;

    // Free memory used by matrices
    cleanupMatrices(pha, phb, phc);
}

// add code here for line x line matrix multiplication
void OnMultLine(int mx_size) {

    SYSTEMTIME Time1, Time2;

    char st[100];
    int i, j, k;

    double *pha, *phb, *phc;

    // Initialize matrices
    setupMatrices(mx_size, &pha, &phb, &phc);

    Time1 = clock();

    for (i = 0; i < mx_size; ++i)
        for (k = 0; k < mx_size; ++k)
            for (j = 0; j < mx_size; ++j)
                phc[i * mx_size + j] += pha[i * mx_size + k] * phb[k * mx_size + j];

    Time2 = clock();
    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for (i = 0; i < 1; ++i)
        for (j = 0; j < min(10, mx_size); ++j)
            cout << phc[j] << " ";
    cout << endl;

    // Free memory used by matrices
    cleanupMatrices(pha, phb, phc);
}

// add code here for block x block matrix multiplication
void OnMultBlock(int mx_size, int bkSize) {
    SYSTEMTIME Time1, Time2;

    char st[100];
    int i, j, k;

    double *pha, *phb, *phc;

    // Initialize matrices
    setupMatrices(mx_size, &pha, &phb, &phc);

    Time1 = clock();

    //code for block x block matrix multiplication

    int side_len_blocks = mx_size / bkSize;

    double *block_a, *block_b;

    block_a = new double[bkSize * bkSize];
    block_b = new double[bkSize * bkSize];

    for (i = 0; i < bkSize * bkSize; ++i)
        block_a[i] = block_b[i] = 0;

    for (int block_row_c = 0; block_row_c < side_len_blocks; ++block_row_c) {
        for (int block_col_c = 0; block_col_c < side_len_blocks; ++block_col_c) {
            
            // with this we select the block in the destiny matrix
            int block_y_offset_c = block_row_c * bkSize;
            int block_x_offset_c = block_col_c * bkSize;

            // since this is a matrix multiplication, we have to traverse the matrix side in blocks

            for (int block_k = 0; block_k < side_len_blocks; ++block_k) {
                int row, col;

                int x_a, y_a, x_b, y_b;

                // pick the block in matrix A
                int block_row_a = block_row_c;
                int block_col_a = block_k;

                // pick the block in matrix B
                int block_row_b = block_k;
                int block_col_b = block_col_c;

                // the computed offset of Matrix A's block
                int block_x_offset_a = block_col_a * bkSize;
                int block_y_offset_a = block_row_a * bkSize;

                // the computed offset of Matrix B's block
                int block_x_offset_b = block_col_b * bkSize;
                int block_y_offset_b = block_row_b * bkSize;

                // TODO: instead of populating the blocks, we could spend some more time thinking about this and figure out how to do the product straight away

                // populate blocks
                for (row = 0; row < bkSize; ++row){
                    for (col = 0; col < bkSize; ++col) {

                        y_a = block_y_offset_a + row;
                        x_a = block_x_offset_a + col;

                        y_b = block_y_offset_b + row;
                        x_b = block_x_offset_b + col;

                        block_a[row * bkSize + col] = pha[y_a * mx_size + x_a];
                        block_b[row * bkSize + col] = phb[y_b * mx_size + x_b];
                    }
                }

                for (i = 0; i < bkSize; ++i)
                    for (k = 0; k < bkSize; ++k)
                        for (j = 0; j < bkSize; ++j) {

                            int x_c, y_c;

                            y_c = block_y_offset_c + i;
                            x_c = block_x_offset_c + j;

                            phc[y_c * mx_size + x_c] += block_a[i * bkSize + k] * block_b[k * bkSize + j];
                        }
            }
        }
    }

    delete[] block_a;
    delete[] block_b;

    Time2 = clock();
    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    // display 10 elements of the result matrix tto verify correctness
    cout << "Result matrix: " << endl;
    for (i = 0; i < 1; ++i)
        for (j = 0; j < min(10, mx_size); ++j)
            cout << phc[j] << " ";

    cout << endl;

    // Free memory used by matrices
    cleanupMatrices(pha, phb, phc);
}

void handle_error(int retval) {
    printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
    exit(1);
}

void runStats(int &EventSet, int &ret, long long values[]) {

    printf("-----Regular Multiplication-----\n\n");

	for (size_t n = 600; n <= 3000; n+=400) {	
		printf("n=%zu\n", n);
		// Start counting
        ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
		OnMult(n);  
  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("PAPI_L1_DCM: %lld \n",values[0]);
  		printf("PAPI_L1_ICM: %lld \n",values[1]);
		printf("PAPI_L2_DCM: %lld \n",values[2]);
  		printf("PAPI_L2_ICM: %lld \n",values[3]);  		
		printf("PAPI_L1_TCM: %lld \n",values[4]);
  		printf("PAPI_L2_TCM: %lld \n",values[5]); 
  		printf("PAPI_TOT_INS: %lld \n\n",values[6]);
		printf("----\n");

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 

	}

    printf("-----Line Multiplication-----\n\n");

	for (size_t n = 600; n <= 3000; n+=400) {	
		printf("n=%zu\n", n);
		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
		OnMultLine(n);  
  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("PAPI_L1_DCM: %lld \n",values[0]);
  		printf("PAPI_L1_ICM: %lld \n",values[1]);
		printf("PAPI_L2_DCM: %lld \n",values[2]);
  		printf("PAPI_L2_ICM: %lld \n",values[3]);  		
		printf("PAPI_L1_TCM: %lld \n",values[4]);
  		printf("PAPI_L2_TCM: %lld \n",values[5]); 
  		printf("PAPI_TOT_INS: %lld \n\n",values[6]);
		printf("----\n");

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 

	}

	for (size_t n = 4096; n <= 10240; n+=2048)  {	
		printf("n=%zu\n", n);

		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
		OnMultLine(n);  
  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("PAPI_L1_DCM: %lld \n",values[0]);
  		printf("PAPI_L1_ICM: %lld \n",values[1]);
		printf("PAPI_L2_DCM: %lld \n",values[2]);
  		printf("PAPI_L2_ICM: %lld \n",values[3]);  		
		printf("PAPI_L1_TCM: %lld \n",values[4]);
  		printf("PAPI_L2_TCM: %lld \n",values[5]); 
  		printf("PAPI_TOT_INS: %lld \n\n",values[6]);
		printf("----\n");

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 

	}

	printf("-----Block Multiplication-----\n\n");

	for (size_t n = 4096; n <= 12400; n+=2048) {
		for (size_t blockSize = 128; blockSize <= 512; blockSize *= 2) {
			printf("n=%zu, blocksize=%zu\n", n, blockSize);
			// Start counting
			ret = PAPI_start(EventSet);
			if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
			OnMultBlock(n, blockSize);  
			ret = PAPI_stop(EventSet, values);
			if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
			printf("PAPI_L1_DCM: %lld \n",values[0]);
			printf("PAPI_L1_ICM: %lld \n",values[1]);
			printf("PAPI_L2_DCM: %lld \n",values[2]);
			printf("PAPI_L2_ICM: %lld \n",values[3]);  		
			printf("PAPI_L1_TCM: %lld \n",values[4]);
			printf("PAPI_L2_TCM: %lld \n",values[5]); 
			printf("PAPI_TOT_INS: %lld \n\n",values[6]);
			printf("----\n");
			ret = PAPI_reset( EventSet );
			if ( ret != PAPI_OK )
				std::cout << "FAIL reset" << endl; 
		}
	}
}

void init_papi() {
    int retval = PAPI_library_init(PAPI_VER_CURRENT);
    
    if (retval != PAPI_VER_CURRENT && retval < 0) {
        printf("PAPI library version mismatch!\n");
        exit(1);
    }

    if (retval < 0)
        handle_error(retval);

    std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
              << " MINOR: " << PAPI_VERSION_MINOR(retval)
              << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

int main(int argc, char *argv[]) {

    int mx_size, blockSize;
    int op;

    int EventSet = PAPI_NULL;
    long long values[7] = {0};
    int ret;

    init_papi();

    ret = PAPI_create_eventset(&EventSet);
    if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L1_DCM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


    ret = PAPI_add_event(EventSet, PAPI_L1_TCM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_TCM" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L1_ICM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_ICM" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L2_DCM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L2_TCM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_TCM" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L2_ICM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_ICM" << endl;

	ret = PAPI_add_event(EventSet,PAPI_TOT_INS);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_TOT_INS" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L1_DCH);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCH" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L2_DCH);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCH" << endl;

    op = 1;

    do {
        cout << endl
             << "1. Multiplication" << endl;
        cout << "2. Line Multiplication" << endl;
        cout << "3. Block Multiplication" << endl;
        cout << "4. Run all stats" << endl;
        cout << "0. Quit" << endl;
        cout << "Selection?: ";
        cin >> op;

        if (op == 0) break;
        
        if (op != 4) {
            printf("Dimensions: lins=cols ? ");
            cin >> mx_size;

            // Start counting
            ret = PAPI_start(EventSet);
            if (ret != PAPI_OK)
                cout << "ERROR: Start PAPI" << endl;
        }

        switch (op) {
        case 1:
            OnMult(mx_size);
            break;
        case 2:
            OnMultLine(mx_size);
            break;
        case 3:
            cout << "Block Size? ";
            cin >> blockSize;
            OnMultBlock(mx_size, blockSize);
            break;
        case 4:
            runStats(EventSet, ret, values);
            break;
        }

        if (op != 4) {
            ret = PAPI_stop(EventSet, values);
            if (ret != PAPI_OK)
                cout << "ERROR: Stop PAPI" << endl;
            printf("L1 DCM: %lld \n", values[0]);
            printf("L1 TCM: %lld \n", values[1]);
            printf("L1 ICM: %lld \n", values[2]);
            printf("L2 DCM: %lld \n", values[3]);
            printf("L2 TCM: %lld \n", values[4]);
            printf("L2 ICM: %lld \n", values[5]);
            printf("TOT_INS: %lld \n", values[6]);
            printf("L1 DCH: %lld \n", values[7]);
            printf("L2 DCH: %lld \n", values[8]);

            ret = PAPI_reset(EventSet);
            if (ret != PAPI_OK)
                std::cout << "FAIL reset" << endl;
        }

    } while (op != 0);

    ret = PAPI_remove_event(EventSet, PAPI_L1_DCM);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L1_DCM" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L1_TCM);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L1_TCM" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L1_ICM);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L1_ICM" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L2_DCM" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L2_TCM);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L2_TCM" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L2_ICM);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L2_ICM" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_TOT_INS);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_TOT_INS" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L1_DCH);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L1_DCH" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L2_DCH);
    if (ret != PAPI_OK) std::cout << "FAIL remove event: PAPI_L2_DCH" << endl;

    ret = PAPI_destroy_eventset(&EventSet);
    if (ret != PAPI_OK)
        std::cout << "FAIL destroy" << endl;
}
