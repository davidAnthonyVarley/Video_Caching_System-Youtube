package main.java;

import java.io.*;
import java.util.*;

//import main.java.*;



public class ReadInput {
    public Map<String, Object> data;
    public EndPoint[] endpoints;
    public Cache[] caches;
    public Video[] videos;

    public List<Fitness> population_of_fitness_scores;
    public WeightedDigraph digraph;

    Random random_number_generator;

    private float occupancy_of_caches;

    void cost_of_downloading_from_dc_for_endpoints() {

        for (int i = 0; i < endpoints.length; i++) {
            System.out.println("Endpoint " + i + ":");
            endpoints[i].cost_of_downloading_from_dc();
        }

    }

    public Cache[] dupelicate_caches(ReadInput ri) {
        Cache[] dupelicate_caches = new Cache[ri.caches.length];


        for (int i = 0; i < ri.caches.length; i++) {
            Cache dupe = new Cache(ri.caches[i].total_capacity, ri.caches[i].ID);
            dupelicate_caches[i] = dupe;
        }

        return dupelicate_caches;

    }

    boolean display_overflowing_caches(Cache[] caches) {

        boolean res = false;

        for (int i = 0; i < caches.length; i++) {
            System.out.println("Cache " + i +":");
            if (caches[i].cache_is_overflowing()) {
                res = true;
            }
        }

        return res;
    }

    void clear_caches() {

        Cache[] newcaches = new Cache[caches.length];
        
        for (int i = 0; i < caches.length; i++) {
            Cache old = caches[i];
            newcaches[i] = new Cache(old.total_capacity, i);
        }

        //created new array of empty caches
        caches = newcaches;

    }


    void display_occupancy_of_caches() {

        int number_of_vids = (int) data.get("number_of_videos");

        String top_line = "           | ";
        for (int i = 0; i < number_of_vids; i++) {
            top_line += Integer.toString(i) + " | ";
        }

        String divider = "";
        for (int i = 0; i < ((number_of_vids*5) + 13); i++) {
            divider += "-";
        }

        int[][] data = new int[caches.length][number_of_vids];

        
        for (int i = 0; i < caches.length; i++) {

            int[] cache_vids = new int[number_of_vids];

            Set<Integer> arr = getCacheByID(i).videos.keySet();

            for (Integer vid_id : arr) {
                int id = vid_id;
                cache_vids[id] = 1;
            }

            data[i] = cache_vids;

            String line = "Cache ";

            if (i > 999) {
                line += Integer.toString(i);
            }
            else if ( i > 99) {
                line += " " +Integer.toString(i);
            }
            else if ( i > 9) {
                line += "  " +Integer.toString(i);
            }
            else {
                line += "   " +Integer.toString(i);
            }

            line += " | ";

            for (int j = 0; j < number_of_vids; j++) {
                line += Integer.toString(cache_vids[j]) + " | ";
            }

            System.out.println(divider);
            System.out.println(line);


        }

        //write_to_excel writer = new write_to_excel();
        //write_to_excel.write(data);

        System.out.println("Caches contain " + occupancy_of_caches*100 + "% of videos ");

    }

    public PriorityQueue<RequestDescription> requested_videos;

    void display_requested_videos_of_priority_queue() {

        System.out.println("\n");
            System.out.println("The video requests in descending order: ");

            PriorityQueue<RequestDescription> copy = new PriorityQueue<>(this.requested_videos);

            while (!copy.isEmpty()) {
                RequestDescription rd = copy.poll();
                Video v = getVideoByID(rd.video_id);
                System.out.println(rd.number_of_requests + " requests for video " + v.ID + ", with a video size of " + v.size + "MB from endpoint " + rd.endpoint_id);
            }
            
            //System.out.println("Number of requests: " + data.get("number of requests"));
            System.out.println("\n");

    }

    private class RequestDescription {
        public int number_of_requests;
        public int video_id;
        public int endpoint_id;

        public int productivity;
        //belongs to ep object, so no need for ep id

        public RequestDescription(int number_of_requests, int video_id, int endpoint_id) {
            this.number_of_requests = number_of_requests;
            this.video_id = video_id;
            this.endpoint_id = endpoint_id;

            //THIS IS FOR PART 3 + PART 4
            //this.productivity = number_of_requests / getVideoByID(video_id).size;

            //THIS IS FOR GENETIC ALGORITHM
            this.productivity = random_number_generator.nextInt(1000000);

            //System.out.println("random num: " + this.productivity);
            //give it a random productivity score, so vids will be randomly allocated into pq
        }

    }

    private class RequestComparator implements Comparator<RequestDescription> {
        @Override
        public int compare(RequestDescription desc1, RequestDescription desc2) {
            // Compare based on the number of requests (descending order)
            return Integer.compare(desc2.productivity, desc1.productivity);
            //return Integer.compare(desc2.number_of_requests, desc1.number_of_requests);
        }

     
    }

    void put_requested_video(int num_of_requests, int video_id, int endpoint_id) {
        //RequestDescription rd = new RequestDescription( num_of_requests, video_id);
        requested_videos.offer( new RequestDescription( num_of_requests, video_id, endpoint_id) );
    }


    private class Video {

        public int ID;
        public int size;
        public int endpoint_id;

        public Video( int id, int size) {
            this.ID = id;
            this.size = size;
        }


        private class VideoComparator implements Comparator<Video> {
            @Override
            public int compare(Video v1, Video v2) {
                // Compare based on the number of requests (descending order)
                return Integer.compare(v2.size, v1.size);
            }
    
         
        }


    }

    public Video getVideoByID(int id) {
        return videos[id];
    }
    public Cache getCacheByID(int id) {
        return caches[id];
    }
    public EndPoint getEndpointByID(int id) {
        return endpoints[id];
    }

    void print_endpoints() {
        for ( EndPoint e :  endpoints) {
            e.display_closest_relevant_caches();
        }
    }

    public class EndPoint {

        public List<Integer> cache_server_latencies;
        
        public int ID;
        
        public int endpoint_to_data_centre_latency;
        public List<Integer> closest_relevant_caches;

        public EndPoint(int ep_to_dc_latency, int ID) {
            this.cache_server_latencies = new ArrayList<Integer>();
            // a max pq that sorts by number of requests
           
            this.closest_relevant_caches = new ArrayList<Integer>();
            this.ID = ID;

            this.endpoint_to_data_centre_latency = ep_to_dc_latency;
        }

        void cost_of_downloading_from_dc() {
            System.out.println("Cost of downloading from dc: " + this.endpoint_to_data_centre_latency);
        }

        int average_cost_of_using_cache_server() {
            int sum = 0;

            for (Integer lat : cache_server_latencies) {
                sum += lat;
            }

            return sum / cache_server_latencies.size();
        }

        public int get_latency_of_cache_to_this_endpoint(int cache_id) {

            boolean found = false;
            int latency = -1;

            //System.out.println( "Cache id: " + cache_id);
            //System.out.println( "Caches: " + closest_relevant_caches);

            for (int i =0; i < closest_relevant_caches.size(); i++) {
                if ( closest_relevant_caches.get(i) == cache_id) {

                    latency = cache_server_latencies.get(i);
                    found = true;
                }
            }

            //System.out.println( "Latency of cache " + cache_id + " to endpoint " + this.ID +": " +  latency);

            return latency;

        }

        public void display_closest_relevant_caches() {
            System.out.println("The closest caches' ID, from closest to furthest, of relevant cache servers for endpoint " + this.ID + ": "); 
            // for (Integer i : this.closest_relevant_caches) {
            //     System.out.println(i); 
            // }

            int len = this.closest_relevant_caches.size();

            for (int i = 0; i < len; i++) {

                Integer index = this.closest_relevant_caches.get(i);

                if ( i == len -1) {
                    System.out.println(index);
                }
                else {
                    System.out.print(index + ", ");
                }
                
            }
        }
         
    }

    private class Cache {

        private int total_capacity;
        public int capacity_remaining; //in mb
        private Map<Integer, Video> videos;
        public int ID;
        //public Map video_id

        public Cache(int total_capacity, int id) {
            this.capacity_remaining = total_capacity;
            this.total_capacity = total_capacity;
            this.ID = id;

            this.videos = new HashMap<Integer, Video>();
        }

        public boolean has_capacity_for_video(int vid_size) {
            //System.out.println("does the cache have capacity with remaining " + this.capacity_remaining + " - vid size of " + vid_size);
            return ((capacity_remaining - vid_size) >= 0 ) ? true : false;
        }

        private boolean contains_video(Video v) {
            return videos.containsKey(v.ID);
        }

        void add_video( Video v) {

            //whether the cache has capacity for the vid is already checked
            //make sure we don't already have the vid stored there, if so, just do nothing
            if ( !contains_video(v)) {
                videos.put(v.ID, v);
                this.capacity_remaining -= v.size;
            }
        }

        public boolean cache_is_overflowing() {

            int occupancy = 0;

            for (Video vid : videos.values()) {

                occupancy += vid.size;

            }

            float occ = (float) occupancy;

            System.out.println("Total occupancy is  " + ( (occ / (float) total_capacity) *100) + "%");

            return occupancy > total_capacity;



        }


        //checks if a solution resulting from a swap is feasible
       

        int number_of_videos() {
            return videos.size();
        }


    }

    void display_remaining_capacity_of_caches() {

        int sum = 0;
       
        for (int i = 0; i < caches.length; i++) {

            int left = caches[i].capacity_remaining;
            sum+=left;
            System.out.println("Remaining capacity of cache " + i + ": " + left + "MB");
        }
        //all caches have same capaacity
        System.out.println("---------------------------------------- ");
        System.out.println("Cumulative remaining capacity of all caches: " + sum + "MB == " + (( (float) sum / (float) (caches.length * caches[0].total_capacity)  )*100) + "%");


    }

    boolean is_solution_feasible(int[][][] solution) {

        for (int i = 0; i < solution.length;  i++) {
            //System.out.print("\n");
            int cache_sum = 0;
            
            for (int j = 0; j < solution[i].length; j++) {

                // [0] contains whether or not it is cached
                // [1] contains the total savings
                if (solution[i][j][0] == 1) {
                    cache_sum += getVideoByID(j).size; //solution[i][j];
                    //System.out.println( "video " + j + "has " +getVideoByID(j).size + " ");
                }

            }

            //System.out.println("all the videos in the current cache sums to: " + cache_sum); 

            if ( cache_sum > getCacheByID(i).total_capacity) {
                return false;
            }

        }

        //otherwise, we have checked every row of the solution, and it never overflows
        return true;

    }



    void parse_request_descriptions_for_max_pq() {
        String rd = (data.get("video_ed_request")).toString();

        //data is stored as {... vidID,epID=num_of_requests, ...}
      
        String charsToRemove = "{},="; // Characters to remove
        char replacementChar = ' '; // Character to replace with
        rd = rd.replaceAll("[" + charsToRemove + "]", String.valueOf(replacementChar));
        charsToRemove = "  ";
        rd = rd.replace("  ", " ");
        //System.out.println(rd); 

        String[] numbers_as_strings = rd.split("\\s+");
        
        if (numbers_as_strings[0].equals("")) {

            String[] temp = new String[numbers_as_strings.length -1];
            for (int i = 1; i < numbers_as_strings.length; i++) {
                temp[i-1] = numbers_as_strings[i];
            }

            numbers_as_strings = temp;
        }
        

        /* 

        int i = numbers_as_strings[0].equals("") ? 1 : 0;
        int j = 0;

        for (; i < numbers_as_strings.length; i+=3, j++) {
            System.out.println(j + ": " +numbers_as_strings[i]+ ", " + numbers_as_strings[i+1] + ", " +numbers_as_strings[i+2]); 
        }

        i = numbers_as_strings[0].equals("") ? 1 : 0;

        for (; i < numbers_as_strings.length; i+=3) {
            System.out.println(numbers_as_strings[i+2] +" requests for video " + numbers_as_strings[i] + " coming from endpoint " +numbers_as_strings[i+1]+"."); 
        }

        //*/

        
        //convert all strings into integer objects
        int[] numbers_as_ints= new int[numbers_as_strings.length];
        



        for ( int index = 0; index < numbers_as_strings.length; index++) {
            numbers_as_ints[index] = Integer.parseInt(numbers_as_strings[index]);
        }

        add_request_descriptions_to_global_pq(numbers_as_ints);
        //look for
    }
    void add_request_descriptions_to_global_pq(int[] numbers) {

        for (int i = 0; i < numbers.length; i+=3) {
            //Endpoint ep = 
            int ep_id = numbers[i+1];
            int vid_id = numbers[i];
            int num_of_requests = numbers[i+2];

            put_requested_video(num_of_requests, vid_id, ep_id);

            //System.out.println(": " +numbers[i]+ ", " + numbers[i+1] + ", " +numbers[i+2]); 
        
        }

    }

    /*

    int[][] project_specified_hill_climbing_algorithm(int number_of_caches, int number_of_videos) {

        int[][] new_solution = new int[number_of_caches][number_of_videos];

        int number_of_iterations = ( number_of_caches * number_of_videos * 2);
        int c = random_number_generator.nextInt(number_of_caches -1);
        int v = random_number_generator.nextInt(number_of_videos -1);

        for (int i = 0; i < number_of_iterations; i++) {

            //initialise
            int[] greatest_savings = {Integer.MAX_VALUE, c+1, v+1};

            

            int[][] cells = { 
                {c-1, v-1}, 
                {c-1, v}, 
                {c-1, v+1}, 

                {c, v-1}, 
                {c, v}, 
                {c, v+1}, 

                {c+1, v-1}, 
                {c+1, v}, 
                {c+1, v+1}, 
        
            };

            for (int j = 0; j < 9; j++) {
                
                    //if cache index is out of bounds
                    if ( (cells[j][0] >= 0) && ( cells[j][0] < number_of_caches) ) {
                        //if video index is out of bounds
                        if ( (cells[j][1] >= 0) && ( cells[j][1] < number_of_videos)) {

                                if ( new_solution[cells[j][1]][cells[j][2]] != 1 ) {

                                int latency = caculate_latency_savings(cells[j][0], cells[j][1]);
                                    if (latency < greatest_savings[0]) {
                                        int[] temp = { latency, cells[j][0], cells[j][1]};
                                        greatest_savings = temp;
                                    }

                            }
                        }
                    }

            }

            if (greatest_savings[0] == Integer.MAX_VALUE) {
                greatest_savings[0] = 0;
            }

            new_solution[greatest_savings[1]][greatest_savings[2]] = 1;

        }



        return new_solution;
    }

    int caculate_latency_savings(int cache_id, int video_id) {

        EndPoint ep = getEndpointByID( getVideoByID(video_id).endpoint_id);
        int latency = ep.get_latency_of_cache_to_this_endpoint(cache_id);
        if (latency == -1) {
            return Integer.MAX_VALUE;
        }
        else {
            return latency;
        }


    }

    */



    //goal of this is to serve the largest number of requests, not to serve an equal number of requests from each endpoint
    void enter_videos_into_caches() {

        float count = 0;
        float og_size = (float) requested_videos.size();

        int cost_of_downloading_every_vid_from_dc = 0;
        int cost_of_downloading_from_cache_server = 0;

        int total_savings_across_all_requests = 0;

        int total_number_of_requests_cached = 0;
        int total_number_of_requests = 0;

        PriorityQueue<RequestDescription> dupe = new PriorityQueue<>(requested_videos);
        int[][][] solution = new int[caches.length][ (int) data.get("number_of_videos")][2];

        while ( !requested_videos.isEmpty() ) {

            RequestDescription current_max_requests = requested_videos.poll();
            EndPoint ep = getEndpointByID(current_max_requests.endpoint_id);

            //For each element in the field “video ed request”, compute the cost of downloading it from the data centre
   

            Video video = getVideoByID(current_max_requests.video_id);
            int video_size = video.size;

            List<Integer> caches = ep.closest_relevant_caches;

            boolean allocated = false;

            int cost_of_downloading_from_dc_for_this_video = ep.endpoint_to_data_centre_latency;
            //System.out.println("Productivity: " + current_max_requests.productivity);
            int average_cost_of_downloading_from_cache_server_for_this_video = cost_of_downloading_from_dc_for_this_video;
            //System.out.println("Productivity: " + current_max_requests.productivity);

            //System.out.println(video_size + "MB video ");
           // System.out.println("Caches: " + caches);
            for (int c = 0; c< caches.size() && !allocated; c++) {

                Cache current_cache = getCacheByID(caches.get(c));

                //System.out.print(current_cache.capacity_remaining + "MB remaining ");
                if (current_cache.has_capacity_for_video(video_size) ) {

                    average_cost_of_downloading_from_cache_server_for_this_video = ep.get_latency_of_cache_to_this_endpoint(current_cache.ID);

                    total_number_of_requests_cached += current_max_requests.number_of_requests;

                    if (!current_cache.contains_video(video)) {
                        solution[current_cache.ID][current_max_requests.video_id][0] = 1;//video_size;
                        solution[current_cache.ID][current_max_requests.video_id][1] = (cost_of_downloading_from_dc_for_this_video - average_cost_of_downloading_from_cache_server_for_this_video) * current_max_requests.number_of_requests;//video_size;
                    }


                    current_cache.add_video(video);

                    
                   
                    allocated = true;
                    count += 1;

                    //System.out.println("Allocated video " + video.ID + " to cache " + c);
                }

            }
            if (!allocated) {

                //System.out.println("Could not allocate a video to a cache");
            }

            cost_of_downloading_every_vid_from_dc += cost_of_downloading_from_dc_for_this_video;
            cost_of_downloading_from_cache_server += average_cost_of_downloading_from_cache_server_for_this_video;

            
            int individual_savings_from_using_cache = cost_of_downloading_from_dc_for_this_video - average_cost_of_downloading_from_cache_server_for_this_video;
            int total_savings = individual_savings_from_using_cache * current_max_requests.number_of_requests;
           

            total_savings_across_all_requests += total_savings;
            total_number_of_requests += current_max_requests.number_of_requests;

        }

        Fitness fitness = new Fitness(solution, (double) total_savings_across_all_requests, (double) total_number_of_requests_cached, (double) total_number_of_requests);
        population_of_fitness_scores.add(fitness);
        //display_remaining_capacity_of_caches();

        //to view the fitness scores, uncomment the line below
        //fitness.scores();

        clear_caches();

        requested_videos = dupe;

        

    }

    int find_fitness_score_of_solution(int[][][] solution) {

        int total_savings_across_all_requests = 0;

        //int total_number_of_requests_cached = 0;
        //int total_number_of_requests = 0;


        for (int i = 0; i < solution.length; i++) {

            for (int j = 0; j < solution[i].length; j++) {
                
                total_savings_across_all_requests += solution[i][j][1];

            }
        }

        
        return total_savings_across_all_requests;

    }

    



    public class Fitness {

        public int[][][] solution;
        public double total_savings_across_all_requests;
        public double total_number_of_requests_cached;
        public double total_number_of_requests;

        //will always be in microseconds
        public double fitness;
        public double allocation;

        //remembers how the caches were allocated with this solutoin
        public Cache[] caches_blueprint;

        //public Fitness() { }
        public Fitness(int[][][] solution, double total_savings_across_all_requests, double total_number_of_requests_cached, double total_number_of_requests) {

            this.solution = solution;
            this.total_savings_across_all_requests = total_savings_across_all_requests;
            this.total_number_of_requests_cached = total_number_of_requests_cached;
            this.total_number_of_requests = total_number_of_requests;

            this.caches_blueprint = caches.clone();

        }

        public void scores() {
            // sum all gains and divide by the number of individual requests (not the field “number of requests”). Look at the example given at the end of the file “ReadInput.java”
            
            //(1500 · 700 + 500 · 0 + 1000 · 800 + 1000 · 0)/(1500 + 500 + 1000 + 1000)
            this.fitness = this.total_savings_across_all_requests / this.total_number_of_requests;
            //System.out.println("Total savings: " + this.total_savings_across_all_requests);
            //System.out.println("Total number of requests: " + this.total_number_of_requests);
            //System.out.println("\nThe actual gains (fitness score) from using cache servers for each request, in every request description, is " + fitness + ", ie, everytime a user requests to see a video, we save " + fitness + " mili-seconds by using cache");
            
            //which equals 462.5ms. Multiplied by 1000, this gives the score of 462 500 .
            this.fitness *= 1000;
            //System.out.println("On average, we save " + this.fitness + " micro seconds on every request by using caches");
            System.out.println("Fitness: " + this.fitness);
    
            this.allocation = this.total_number_of_requests_cached / this.total_number_of_requests;
            allocation *= 100;
    
            //System.out.println("\nAllocated " +  this.occupancy_of_caches + "% of videos to caches");
            //System.out.println("\nAllocated " +  (this.allocation) + "% of individual requests to caches");
            //System.out.println("Allocation: " +  (this.allocation) + "% of individual requests to caches");
            
            //System.out.println("The average cost of downloading from a cache is " + cost_of_downloading_from_cache_server);
        }

        void display_absolute_savings() {
            System.out.println("Absolute savings of this solution: " + this.total_savings_across_all_requests);
        }

        void display_fitness() {
            System.out.println("Fitness of this solution: " + 1000 *(this.total_savings_across_all_requests / this.total_number_of_requests));
        }



    }

    private class FitnessComparator implements Comparator<Fitness> {
        @Override
        public int compare(Fitness desc1, Fitness desc2) {
            // Compare based on the number of requests (descending order)
            return Double.compare(desc2.total_savings_across_all_requests, desc1.total_savings_across_all_requests);
            //return Integer.compare(desc2.number_of_requests, desc1.number_of_requests);
        }

     
    }


    public ReadInput() {
        data = new HashMap<String, Object>();
        this.requested_videos = new PriorityQueue<>(new RequestComparator());
        this.population_of_fitness_scores = new ArrayList<Fitness>();
        this.random_number_generator = new Random(456);
        //this.digraph = new WeightedDigraph(v)
        
    }

    void keep_fittest_solutions(int max_num_of_individuals) {

        List<Fitness> temp = new ArrayList<Fitness>();

        PriorityQueue<Fitness> pq = new PriorityQueue<>(new FitnessComparator());

        for (Fitness f : this.population_of_fitness_scores) {
            pq.offer(f);
        }

        //keep 50 fittest individuals

        for (int i = 0; i < max_num_of_individuals; i++) {
            temp.add( pq.poll());
        }

        this.population_of_fitness_scores = temp;

         //quicksort, not used
         /* 
        //this.population_of_fitness_scores = Collections.sort(this.population_of_fitness_scores);
        //Collections.sort(this.population_of_fitness_scores);

        //keep 50 fittest individuals

        int i =  this.population_of_fitness_scores.size() -1 ;
        int stop = this.population_of_fitness_scores.size() - max_num_of_individuals;

        for (; i > stop; i--) {
            temp.add( this.population_of_fitness_scores.get(i));
        }

        */

    }

    double get_average_total_savings_across_population_of_fitness_scores() {
        double sum = 0;

        for (Fitness f: this.population_of_fitness_scores) {
            sum += f.total_savings_across_all_requests;
        }

        return sum / this.population_of_fitness_scores.size();
    }


    Fitness[] crossover(int[][][] one, int[][][] two) {

        int[][][] first = one.clone();
        int[][][] second = two.clone();


        for (int i = first.length / 2; i < first.length; i++) {

            for (int j = 0; j < first[i].length; j++) {
                int[] a = first[i][j];
                int[] b = second[i][j];

                first[i][j] = b;
                second[i][j] = a;

            }
        }

        int fsavings = find_fitness_score_of_solution(first);
        int ssavings = find_fitness_score_of_solution(second);

        Fitness fir = new Fitness(first, fsavings, 0, 0);
        Fitness scd = new Fitness(second, ssavings, 0, 0);

        //int[][][][] ret = { first, second };
        Fitness[] ret = { fir, scd};
        return ret;
    }


    void mutate(int crossover_upperbound, int mutation_upperbound) {
        // to generate a probability of 0.1, make an upper bound of 10, for 0.05, 20, etc

        int num_iterations = this.population_of_fitness_scores.size() -1;

        for (int i = 0; i < num_iterations ; i++) {

            int rand = this.random_number_generator.nextInt(crossover_upperbound);

            //chances of this result are 1 / upperbound
            if (rand==5) {
                //crossover
                int[][][] one = this.population_of_fitness_scores.get(i).solution;
                int[][][] two = this.population_of_fitness_scores.get(i+1).solution;

                Fitness[] crossed_over = this.crossover(one, two);

                if (is_solution_feasible(crossed_over[0].solution)) {
                    this.population_of_fitness_scores.add(crossed_over[0]);
                    mutate_child(crossed_over[0], mutation_upperbound); // will only change solution if the mutation is valid
                }

                if (is_solution_feasible(crossed_over[1].solution)) {
                    this.population_of_fitness_scores.add(crossed_over[1]);
                    mutate_child(crossed_over[1], mutation_upperbound);
                }

                
                
               

                
                

                }
        }
    }

    void mutate_child(Fitness f, int mutation_upperbound) {


        int[][][] temp = f.solution.clone();

        for (int i = 0; i < f.solution.length; i++) {
            for (int j = 0; j < f.solution[0].length; j++) {

                int rand = this.random_number_generator.nextInt(mutation_upperbound);

                // 2 in (V*C) chance
                if (rand==5 || rand==6) {
                    if (temp[i][j][0] == 1 ) {
                        temp[i][j][0] = 0;
                    }
                    else {
                        temp[i][j][0] = 1;
                    }
                }
            }
        }

        if (this.is_solution_feasible(temp) ) {
            //passed by reference so will change the value of object
            f.solution = temp;
        }
    }

    public void readGoogle(String filename) throws IOException {
             
        BufferedReader fin = new BufferedReader(new FileReader(filename));
    
        String system_desc = fin.readLine();
        String[] system_desc_arr = system_desc.split(" ");

        int number_of_videos = Integer.parseInt(system_desc_arr[0]);
        this.digraph = new WeightedDigraph(number_of_videos);

        int number_of_endpoints = Integer.parseInt(system_desc_arr[1]);
        endpoints = new EndPoint[number_of_endpoints];

        int number_of_requests = Integer.parseInt(system_desc_arr[2]);

        int number_of_caches = Integer.parseInt(system_desc_arr[3]);
        int cache_size = Integer.parseInt(system_desc_arr[4]);

        caches = new Cache[number_of_caches];
        for (int i = 0; i < caches.length; i++) {
            caches[i] = new Cache(cache_size, i);
        }

        videos = new Video[number_of_videos];
        
        
        Map<String, String> video_ed_request = new HashMap<String, String>();
        String video_size_desc_str = fin.readLine();
        String[] video_size_desc_arr = video_size_desc_str.split(" ");
        int[] video_size_desc = new int[video_size_desc_arr.length];

        for (int i = 0; i < video_size_desc_arr.length; i++) {

            int num = Integer.parseInt(video_size_desc_arr[i]);
            video_size_desc[i] =num;
            videos[i] = new Video(i, num);
        }
    
        List<List<Integer>> ed_cache_list = new ArrayList<List<Integer>>();
        List<Integer> ep_to_dc_latency = new ArrayList<Integer>();
        List<List<Integer>> ep_to_cache_latency = new ArrayList<List<Integer>>();

        for (int i = 0; i < number_of_endpoints; i++) {

            ep_to_dc_latency.add(0);
            ep_to_cache_latency.add(new ArrayList<Integer>());
    
            String[] endpoint_desc_arr = fin.readLine().split(" ");
            int dc_latency = Integer.parseInt(endpoint_desc_arr[0]);
            int number_of_cache_i = Integer.parseInt(endpoint_desc_arr[1]);
            ep_to_dc_latency.set(i, dc_latency);
    
            for (int j = 0; j < number_of_caches; j++) {
                ep_to_cache_latency.get(i).add(ep_to_dc_latency.get(i) + 1);
            }
    
            List<Integer> cache_list = new ArrayList<Integer>();
            for (int j = 0; j < number_of_cache_i; j++) {
                String[] cache_desc_arr = fin.readLine().split(" ");
                int cache_id = Integer.parseInt(cache_desc_arr[0]);
                int latency = Integer.parseInt(cache_desc_arr[1]);
                cache_list.add(cache_id);
                ep_to_cache_latency.get(i).set(cache_id, latency);
            }
            ed_cache_list.add(cache_list);
        }

        for (int i = 0; i < endpoints.length; i++) {
            endpoints[i] = new EndPoint(ep_to_dc_latency.get(i), i);
        }

        for (int i = 0; i < number_of_endpoints; i++) {

            EndPoint ep_ptr = endpoints[i];

            List<Integer> ep_to_cache_latency_data = ep_to_cache_latency.get(i);
            List<Integer> indexes_to_access_in_data = ed_cache_list.get(i);

            List<Integer> latencies_relevant_to_this_ep = new ArrayList<Integer>( );
            List<Integer> cache_server_ptrs = new ArrayList<Integer>( );

            for (int index = 0; index < indexes_to_access_in_data.size(); index++) {

                Integer latency = ep_to_cache_latency_data.get( indexes_to_access_in_data.get(index) );
                //System.out.println("using index "+ indexes_to_access_in_data.get(index)+ " to get the value "+ latency);
                
                latencies_relevant_to_this_ep.add(latency);
                cache_server_ptrs.add(indexes_to_access_in_data.get(index));
            }



            int iterations = indexes_to_access_in_data.size();
            
            // //sort l
            for (int j = 0; j < iterations; j++) {

                int min_latency = Integer.MAX_VALUE;
                int min_latency_index = -1;

                int index_to_turn_into_infinity = -1;

            //     //get min of remaining lsit, add cache index to this endpoint's list
                for (int k = 0; k < indexes_to_access_in_data.size(); k++) {

                    if (latencies_relevant_to_this_ep.get(k) < min_latency) {
  
                        min_latency = latencies_relevant_to_this_ep.get(k);
                        //here
                        min_latency_index = cache_server_ptrs.get(k);
                        index_to_turn_into_infinity = k;

                    }
                }

                if (min_latency_index==-1) throw new Error();

                //Cache cache_server_ptr = caches[min_latency_index];
                ep_ptr.closest_relevant_caches.add(min_latency_index);
                ep_ptr.cache_server_latencies.add(min_latency);
                //System.out.println("adding cache " + min_latency_index + " to endpoint " +  i);

                //now, it won't add the same index twice
                latencies_relevant_to_this_ep.set(index_to_turn_into_infinity, Integer.MAX_VALUE);
            }



            //ep_ptr.display_closest_relevant_caches();
            //System.out.println(ep_to_cache_latency.get(i));
            //System.out.println();


        }


    
        for (int i = 0; i < number_of_requests; i++) {
            String[] request_desc_arr = fin.readLine().split(" ");
            String video_id = request_desc_arr[0];
            String ed_id = request_desc_arr[1];
            String requests = request_desc_arr[2];
            video_ed_request.put(video_id + "," + ed_id, requests);
        }
    
        data.put("number_of_videos", number_of_videos);
        System.out.println("number_of_videos: "+ number_of_videos);
        System.out.println("\n");

        data.put("number_of_endpoints", number_of_endpoints);
        // System.out.println("number_of_endpoints: "+ number_of_endpoints);
        // System.out.println("\n");

        data.put("number_of_requests", number_of_requests);
        // System.out.println("number_of_requests: "+ number_of_requests);
        // System.out.println("\n");

        data.put("number_of_caches", number_of_caches);
        // System.out.println("number_of_caches: "+ number_of_caches);
        // System.out.println("\n");

        data.put("cache_size", cache_size);
        // System.out.println("cache_size: "+ cache_size);
        // System.out.println("\n");

        data.put("video_size_desc", video_size_desc);
        


        System.out.println("\n");

        data.put("ep_to_dc_latency", ep_to_dc_latency);
        // System.out.println("ep_to_dc_latency: "+ ep_to_dc_latency);
        // System.out.println("\n");

        for (int i = 0; i < ep_to_cache_latency.size(); i++) {
            this.endpoints[i].endpoint_to_data_centre_latency = ep_to_dc_latency.get(i);
        }

        data.put("ed_cache_list", ed_cache_list);
        // System.out.println("ed_cache_list: "+ ed_cache_list);
        // System.out.println("\n");

        data.put("ep_to_cache_latency", ep_to_cache_latency);
        //System.out.println("ep_to_cache_latency: "+ ep_to_cache_latency);
        // for (int i = 0; i < ep_to_cache_latency.size(); i++) {
        //     System.out.println( ep_to_cache_latency.get(i) );
        // }
        // System.out.println("\n");

         data.put("video_ed_request", video_ed_request);
        // System.out.println("video_ed_request: "+ video_ed_request);
        // System.out.println("\n");
        // System.out.println();

        

        fin.close();
     
     }

     @SuppressWarnings("unchecked")
    public String toString() {
        String result = "";

        System.out.println("in the to string func");

        //for each endpoint: 
        for(int i = 0; i < (Integer) data.get("number_of_endpoints"); i++) {
            result += "enpoint number " + i + "\n";
            //latendcy to DC

            Object obj = data.get("ep_to_dc_latency");
            System.out.println("printing out obj: " + obj);


            List<Integer> longlist = (List<Integer>) obj;

            //System.out.println("printing out longlist.get(i): " + longlist.get(i));
            //List<Integer> list = longlist.get(i);

            Integer latency_dc = longlist.get(i);
            result += "latency to dc " + latency_dc + "\n";

            //for each cache
            for(int j = 0; j < ((List<List<Integer>>) data.get("ep_to_cache_latency")).get(i).size(); j++) {
                int latency_c = ((List<List<Integer>>) data.get("ep_to_cache_latency")).get(i).get(j); 
                result += "latency to cache number " + j + " = " + latency_c + "\n";
            }
        }

        return result;
    }

    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> c) {
        List<T> result = new ArrayList<>(c.size());
        for (Object o : c) {
            result.add(clazz.cast(o));
        }
        return result;
    }

    public void allocate_videos_to_caches() {
        parse_request_descriptions_for_max_pq(); //parse_request_descriptions_for_max_pq
        enter_videos_into_caches();
    }
    

    public static void main(String[] args) throws IOException { 
        
        //int[][] temp = { {}};
        //write_to_excel writer = new write_to_excel();
        //write_to_excel.write( temp);

        ReadInput ri = new ReadInput();
        System.out.println("\n\n\n ZOO:");
        ri.readGoogle("input/me_at_the_zoo.in");
        // ri.allocate_videos_to_caches();
        // ri.population_of_fitness_scores.get(0).display_fitness();

        //1.9499988E7 for my 'producitivity' approach
        
        // System.out.println("\n\n\n TRENDING:");
        // ri.readGoogle("input/trending_today.in");
        // System.out.println("\n\n\n KITTENS:");
        // ri.readGoogle("input/kittens.in");

       
        //gen num_of_solutions different random solutions



       
        int num_of_solutions = 200;
        //best for num_of_solutions = 700; 2.136546661E9
        //best for num_of_solutions = 1000; 2.145641257E9


        ///* 
        for (int i = 0; i < num_of_solutions; i++) {
            //System.out.println("\nAllocation " + i);
            ri.allocate_videos_to_caches();
            
        }


        //ri.allocate_videos_to_caches();

        int num_of_generations = 60;
        Fitness f = ri.population_of_fitness_scores.get(0);

        //num caches * num videos
        int mutation_upperbound = f.solution.length * f.solution[0].length;

        for (int i = 0; i < num_of_generations; i++) {

            ri.mutate(num_of_solutions, mutation_upperbound);
            ri.keep_fittest_solutions(num_of_solutions);

        }

        //only bother printing the first 100 and last 100
        for (int i = 0; i < 50; i++) {

            System.out.println(i + ": ");
            //ri.population_of_fitness_scores.get(i).scores();
            //ri.population_of_fitness_scores.get(i).display_absolute_savings();
            ri.population_of_fitness_scores.get(i).display_fitness();
            //System.out.println(ri.is_solution_feasible(ri.population_of_fitness_scores.get(i).solution));
            System.out.println();

        }

        //*/

        


        //ri.display_overflowing_caches();

        //ri.print_endpoints();
        //System.out.println(ri.toString());
        //ri.fitness();

        

        // RequestDescription[] test_ids = new RequestDescription[4];
        // test_ids[0] = ri.requested_videos.poll();
        // test_ids[1] = ri.requested_videos.poll();
        // test_ids[2] = ri.requested_videos.poll();
        // test_ids[3] = ri.requested_videos.poll();
        //ri.test_digraph(test_ids);
        //ri.digraph.digraph_tester();
    }












































































    //IGNORE ALL OF THIS, WAS NOT A VIABLE SOLUTION IN THE END

    void test_digraph(RequestDescription[] RDs) {

        WeightedDigraph temp_digraph = new WeightedDigraph(RDs.length);
        System.out.println(temp_digraph.number_of_caches);

        System.out.println();
        System.out.println("Hill climbing");
        expand(this, temp_digraph, RDs, 0, "");

        temp_digraph.printGraph();
        
        temp_digraph.get_descending_vertices(1, 0);

        //temp_digraph.addEdge(video_ids[], null);



    }

    void expand(ReadInput ri, WeightedDigraph G, RequestDescription[] RDs, int pos, String indent) {

        indent += "\t";
        //System.out.println("pos: " + pos);
        

        //vertex == list of edges, its id is video ids[pos]
        List<Edge> vertex = G.adj.get(pos); 

        Cache[] caches_checkpoint = ri.dupelicate_caches(ri);

        if ( (pos + 1) < RDs.length ) {


            Video vid = ri.getVideoByID( RDs[pos].video_id );
            EndPoint ep = getEndpointByID(RDs[pos].endpoint_id);

            List<Integer> caches_copy = ep.closest_relevant_caches;
            System.out.println(indent+"Number of caches: " + caches_copy.size());
            G.number_of_caches.add(caches_copy.size());

            int index =0;
            for (Integer i : caches_copy) {

                Cache ch = caches_checkpoint[i];
                System.out.println(indent+ "At video " + pos + ", C" + index);
                
                

                if ( ch.has_capacity_for_video( vid.size) ) {

                    ch.add_video(vid);
                    
                    //System.out.println( "Relevant caches: " + ep.closest_relevant_caches);

                    Edge e = new Edge( index, ep.get_latency_of_cache_to_this_endpoint(ch.ID));
                    vertex.add(e);
                    System.out.println(indent+ "Latency cost: " + e.weight);

                    System.out.println();


                    expand(ri, G, RDs, pos+1, indent+"\t");
                }
                
                index++;
            }

            //no need to return anything, it accesses the digraph by reference
        }

    }


    
    public class WeightedDigraph {
        private int V; // Number of vertices
        public List<List<Edge>> adj; // Adjacency list

        //this shows the number of caches for each element at vertex i

        public List<Integer> number_of_caches;
    
        // Class to represent an edge in the graph
    
        // Constructor to initialize the graph with V vertices
        public WeightedDigraph(int V) {
            this.V = V;
            adj = new ArrayList<>(V);
            for (int i = 0; i < V; i++) {
                adj.add(new ArrayList<>());
            }
            this.number_of_caches = new ArrayList<>(V);
        }
    
        // Method to add a weighted edge from vertex v to vertex w
        public void addEdge(int v, int w, int weight) {
            adj.get(v).add(new Edge(w, weight));
        }

        public List<Edge> get_descending_vertices(int vertex_id, int cache_id) {

            int num_of_descendents = this.number_of_caches.get(vertex_id);
            
            List<Edge> whole_list = this.adj.get(vertex_id);
            List<Edge> relevant_list = new ArrayList<Edge>();

            int index = cache_id * num_of_descendents;
            int stop = index + num_of_descendents;

            for (; index < stop; index++) {
                relevant_list.add(whole_list.get(index));
            }

            System.out.println("The edges from vertex " + vertex_id + ", cache " + cache_id + ", going to vertex " + (vertex_id+1) + ": ");
            for (Edge e : relevant_list) {
                e.display(vertex_id + 1);
            }
            return relevant_list;

        }
    
       
    
        public void addEdge(int v, Edge e) {
            adj.get(v).add(e);
        }
    
    
    
        // Method to print the graph
        public void printGraph() {
            for (int i = 0; i < V; i++) {
                System.out.print("Vertex " + i + " is connected to:");
                for (Edge edge : adj.get(i)) {
                    System.out.print(" (" + edge.dest + ", " + edge.weight + ")");
                }
                System.out.println();
            }
        }
    
    
        public void digraph_tester() {
            int V = 5; // Number of vertices
    
            // Create a weighted directed graph with 5 vertices
            WeightedDigraph graph = new WeightedDigraph(V);
    
            // Add weighted edges
            graph.addEdge(0, 1, 5);
            graph.addEdge(0, 2, 3);
            graph.addEdge(1, 3, 7);
            graph.addEdge(2, 3, 2);
            graph.addEdge(3, 4, 4);
    
            // Print the graph
            graph.printGraph();
        }
    
    }


    public class Edge {
        int dest; // Destination vertex
        int weight; // Weight of the edge
    
        public Edge(int dest, int weight) {
            this.dest = dest;
            this.weight = weight;
        }

        void display(int next) {
            System.out.println("Destination: Vertex " + next + ", Cache " + dest + "; Weight: " + weight);
        }


    }











    public static class write_to_excel {
        public static void write(int[][] data) {
            // Sample data
    
            // File path
            String filePath = "ALDS_PROJECT_DATA.csv";
    
            // Write data to the file using FileWriter
            try (FileWriter writer = new FileWriter(filePath)) {
                for (int[] rowData : data) {
                    for (int i = 0; i < rowData.length; i++) {
                        writer.write(Integer.toString(rowData[i]));
                        if (i < rowData.length - 1) {
                            writer.write(','); // Separate values by commas
                        }
                    }
                    writer.write('\n'); // Move to the next line after each row
                }
                System.out.println("Data has been written to " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}