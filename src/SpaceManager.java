import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yurireis on 10/02/17.
 */
public class SpaceManager {
    public List<Ambiente> environments;
    public UICallback callback;
    public JavaSpace space;

    public SpaceManager(UICallback callback){
        this.environments = new ArrayList<>();
        this.callback = callback;
    }

    public void initSpace(){
        try {
            System.out.println("Procurando pelo servico JavaSpace...");
            Lookup finder = new Lookup(JavaSpace.class);
            space = (JavaSpace) finder.getService();

            if (space == null) {
                System.out
                        .println("O servico JavaSpace nao foi encontrado. Encerrando...");
                System.exit(-1);
            }
            System.out.println("O servico JavaSpace foi encontrado.");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void createEnvironment(){
        try{
            LastCreatedEnvironment templateLast = new LastCreatedEnvironment();
            LastCreatedEnvironment lastTaken = (LastCreatedEnvironment) space.take(templateLast,null,60);
            Ambiente template = new Ambiente();
            Ambiente teste = (Ambiente) space.read(template,null,60);
            if (lastTaken == null){
                //Primeiro a ser criado
                LastCreatedEnvironment newEnvCode = new LastCreatedEnvironment();
                newEnvCode.lastCreated = "1";
                Ambiente newEnv = new Ambiente("amb1");
                space.write(newEnvCode,null, Lease.FOREVER);
                space.write(newEnv,null,Lease.FOREVER);
            }
            else{
                LastCreatedEnvironment newEnvCode = new LastCreatedEnvironment();
                newEnvCode.lastCreated = String.valueOf(Integer.parseInt(lastTaken.lastCreated)+1);
                String toBeCreated = "amb" + newEnvCode.lastCreated;
                Ambiente newEnv = new Ambiente(toBeCreated);
                space.write(newEnvCode,null, Lease.FOREVER);
                space.write(newEnv,null,Lease.FOREVER);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        callback.refreshUI();
    }

    public void createDevice(String environmentName){
        try{
            LastCreatedDeviceOnEnv lastDevTemplate = new LastCreatedDeviceOnEnv();
            lastDevTemplate.env = environmentName;
            LastCreatedDeviceOnEnv lastTaken = (LastCreatedDeviceOnEnv) space.take(lastDevTemplate,null,60);
            if (lastTaken == null){
                LastCreatedDeviceOnEnv last = new LastCreatedDeviceOnEnv();
                last.env = environmentName;
                last.lastCreated = "1";
                space.write(last,null,Lease.FOREVER);

                Ambiente existingEnvTemplate = new Ambiente(environmentName);
                Ambiente currentEnv = (Ambiente) space.take(existingEnvTemplate,null,60);
                Dispositivo newDisp = new Dispositivo("disp1");
                if (currentEnv.devices != null){
                    currentEnv.devices.add(newDisp);
                }
                else{
                    currentEnv.devices = new ArrayList<>();
                    currentEnv.devices.add(newDisp);
                }
                space.write(currentEnv,null,Lease.FOREVER);
            }
            else{
                LastCreatedDeviceOnEnv last = new LastCreatedDeviceOnEnv();
                last.env = environmentName;
                last.lastCreated = String.valueOf(Integer.parseInt(lastTaken.lastCreated)+1);
                space.write(last,null,Lease.FOREVER);

                Ambiente existingEnvTemplate = new Ambiente(environmentName);
                Ambiente currentEnv = (Ambiente) space.take(existingEnvTemplate,null,60);
                Dispositivo newDisp = new Dispositivo("disp" + last.lastCreated);
                if (currentEnv.devices != null){
                    currentEnv.devices.add(newDisp);
                }
                else{
                    currentEnv.devices = new ArrayList<>();
                    currentEnv.devices.add(newDisp);
                }
                space.write(currentEnv,null,Lease.FOREVER);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        callback.refreshUI();
    }

    public void deleteEnvironments(String environmentName){
        Ambiente template = new Ambiente(environmentName);
        try {
            Ambiente read = (Ambiente) space.read(template, null,60);
            if (read != null){
                if (read.devices == null || read.devices.size() == 0){
                    space.take(template,null,60);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        callback.refreshUI();
    }

    public List<String> listEnvironments(){
        //Get environments from javaspace and show them
        Ambiente envTest = new Ambiente();
        List<Ambiente> envs = new ArrayList<>();
        while (envTest != null){
            Ambiente envTemplate = new Ambiente();
            try{
                envTest = (Ambiente) space.take(envTemplate,null,60);
                if (envTest != null){
                    envs.add(envTest);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        List<String> toReturn = new ArrayList<>();
        for (int i = 0; i < envs.size(); i ++){
            try {
                space.write(envs.get(i),null,Lease.FOREVER);
                toReturn.add(envs.get(i).name);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return toReturn;
    }

    public List<String> listDevicesFromEnvironment(String environmentName){
        //Read environment and show list of devices
        try {
            Ambiente template = new Ambiente(environmentName);
            Ambiente taken = (Ambiente) space.read(template,null,60);

            if (taken.devices != null){
                List<String> toReturn = new ArrayList<>();
                for (int i = 0;i < taken.devices.size(); i++){
                    toReturn.add(taken.devices.get(i).name);
                }
                return toReturn;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        callback.refreshUI();
        return new ArrayList<>();
    }

    public void moveDevice(String deviceName, String fromEnvironmentName, String toEnvironmentName){
        try{
            Ambiente templateFrom = new Ambiente(fromEnvironmentName);
            Ambiente templateTo = new Ambiente(toEnvironmentName);
            Ambiente fromTaken = (Ambiente) space.take(templateFrom, null, 60);
            Ambiente toTaken = (Ambiente) space.take(templateTo,null,60);
            if (fromTaken != null && toTaken != null){
                if (fromTaken.devices != null){
                    List<Dispositivo> toRemove = new ArrayList<>();
                    Dispositivo toMove = null;
                    for (Dispositivo disp : fromTaken.devices){
                        if (disp.name.equals(deviceName)){
                            toRemove.add(disp);
                            toMove = disp;
                        }
                    }
                    fromTaken.devices.removeAll(toRemove);
                    if (toMove != null){
                        if (toTaken.devices != null){
                            toMove.name = toMove.name + "(*)";
                            toTaken.devices.add(toMove);
                        }
                        else{
                            toTaken.devices = new ArrayList<>();
                            toMove.name = toMove.name + "(*)";
                            toTaken.devices.add(toMove);
                        }
                    }
                }
            }
            space.write(fromTaken,null,Lease.FOREVER);
            space.write(toTaken,null,Lease.FOREVER);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        callback.refreshUI();
    }

    public void deleteDeviceFromEnvironment(String env, String dev){
        try{
            Ambiente template = new Ambiente(env);
            Ambiente taken = (Ambiente) space.take(template,null,60);
            if (taken != null){
                if (taken.devices != null){
                    List<Dispositivo> toRemove = new ArrayList<>();
                    for (Dispositivo disp : taken.devices){
                        if (disp.name.equals(dev)){
                            toRemove.add(disp);
                        }
                    }
                    taken.devices.removeAll(toRemove);
                }
            }
            space.write(taken,null,Lease.FOREVER);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        callback.refreshUI();
    }
}
