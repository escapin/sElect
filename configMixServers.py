import os
import re
import shutil
import sys

template_path="templates"
pattern="config_mix[0-9]+.json"
source_dir="MixServer"
dest_dir="mix"
dest_config_file="config.json"
toBeSymLinked = [ "mixServer.js", "cleanData.sh", "run.sh" ]

# to be run after the MixServer has been configured by the make file
# and the files "config_mix[0-9]+.json" are already in template
def config_mix_servers():
    files = os.listdir(template_path)
    p=re.compile(pattern)
    configMix_files=filter(p.search, files)
    # now only the numbers
    numbers = [re.search('[0-9]+', x).group(0) for x in configMix_files]
    if(len(numbers)!=len(configMix_files)):
        sys.exit("Something went wrong with the pattern matching operators.")
    # create the as many 'MixServer' folders as config files found
    for i in range(0, len(numbers)):
        curr_dir=os.path.join(dest_dir, numbers[i]);
        if(os.path.isfile(curr_dir)):
            os.remove(curr_dirr)
        elif(os.path.isdir(curr_dir)):
            shutil.rmtree(curr_dir)
        os.makedirs(curr_dir)
        # ...and smblink the corresponding config file
        os.symlink(os.path.join("../../", template_path, configMix_files[i]), os.path.join(curr_dir, dest_config_file))
        for file in toBeSymLinked:
            os.symlink(os.path.join("../../", source_dir, file), os.path.join(curr_dir, file));
        print "\tDirectory '" + curr_dir + "' created and symbolic links created inside";

if __name__ == '__main__':
    config_mix_servers()
