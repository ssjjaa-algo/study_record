# Imperative Commands

태그: 2-46

- Deploy a pod named `nginx-pod` using the `nginx:alpine` image.
    
    ```yaml
    kubectl run nginx-pod --image=nginx:alpine
    ```
    

- Deploy a `redis` pod using the `redis:alpine` image with the labels set to `tier=db`.
    
    ```yaml
    kubectl run redis --image=redis:alpine --labels=tier=db
    ```
    

- Create a service `redis-service` to expose the `redis` application within the cluster on port `6379`.
    - expose 키워드를 쓰는 것
    
    ```yaml
    kubcetl expose pod redis --port=6379 --name redis-service
    ```
    

- Create a deployment named `webapp` using the image `kodekloud/webapp-color` with `3` replicas.
    
    ```yaml
    kubectl create deployment webapp --image=kodekloud/webapp-col
    or --replicas=3
    ```
    

- Create a new pod called `custom-nginx` using the `nginx` image and run it on `container port 8080`.
    
    ```yaml
    kubectl run custom-nginx --image=nginx --port=8080
    ```
    

- Create a new namespace called `dev-ns`.
    
    ```yaml
    kubectl create ns dev-ns
    ```
    

- Create a new deployment called `redis-deploy` in the `dev-ns` namespace with the `redis` image. It should have `2` replicas.
    
    ```yaml
    kubectl create deployment redis-deploy --image=redis -n dev-n
    s --replicas=2
    ```
    

- Create a pod called `httpd` using the image `httpd:alpine` in the default namespace. Next, create a service of type `ClusterIP` by the same name `(httpd)`. The target port for the service should be `80`.
    
    ```yaml
    # 파드 만들고 난 뒤 
    kubectl expose pod httpd --port=80 --type=ClusterIP
    
    # 한방에 만드는 법 (Solution)
    kubectl run httpd --image=httpd:alpine --port=80 --expose
    ```
