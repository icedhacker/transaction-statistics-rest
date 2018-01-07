## Design Overview

### Classes

Transaction : A simple POJO to define the format of the input given to the system.

Statistics : A POJO defining the different statistics to be generated from the transaction data. This class provides
a method to add transactions allowing for aggregation of multiple transactions to form the statistics for a particular
second (in our case).

StatisticsAggregator : Provides the storage for the different transactions and the business logic to calculate the
aggregated statistics

### Storage

Statistics are stored in an array format (Cyclic Buffer). This array has a fixed size of 60 and stores the aggregated 
statistics for each second. The data is based on a Base Timestamp / Min Timestamp (decided during initialization) and 
is added in a cyclic way to maintain only the required 60 records.

The following formula helps to find the index of a record for Timestamp T (in secs) :
```
int index = (T - baseTimestamp) % 60
```
   
To make sure the data is reset to contain only the required 60 records, we will be resetting this array on every read / 
write request. For deciding the data that needs to be reset / deleted for Timestamp T (in secs), we use the following 
logic : 
```
int currentMin = T - 59;
int diff = currentMin - minTimestamp;
```
Using this diff, we decide on the index range that needs to be deleted / reset :
1. If the diff is equal to 0, then no change is required.
2. If the diff is greater than or equal to 60, then the whole array has to be refreshed.
3. If the diff is less than 60, the start and end index to be reset has to calculated.
```
int start = (int) (minTimestampInSecs - baseTimestampSecs) % 60;
int end = (int) (currentMinTimestamp - baseTimestampSecs) % 60;
```

Hope this gives a clear idea about the storage and retrieval logic.
