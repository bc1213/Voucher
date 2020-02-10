//
//  AuthViewController.swift
//  Voucher tvOS App
//
//  Created by Rizwan Sattar on 11/7/15.
//  Copyright © 2015 Rizwan Sattar. All rights reserved.
//

import UIKit
import Voucher

class AuthViewController: UIViewController, VoucherClientDelegate {

    var delegate: AuthViewControllerDelegate?
    var client: VoucherClient?
    var randomInt = 0;
    var deviceInfo:[String:NetService]?
    var devices:Array<NetService>?
    var pureDevices:Array<NetService>?
    let stringToRemove = ["#Android", "#IOS"]

    @IBOutlet weak var searchingLabel: UILabel!
    @IBOutlet weak var connectionLabel: UILabel!
    @IBOutlet weak var deviceTableView: UITableView!

    deinit {
        self.client?.stop()
        self.client?.delegate = nil
        self.client = nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.deviceTableView.delegate = self
        self.deviceTableView.dataSource = self
        
        self.deviceTableView.register(UITableViewCell.self, forCellReuseIdentifier: "Cell")

        randomInt = Int.random(in: 1000..<9999)
        let uniqueId = String(randomInt)+"VoucherTest";
        self.client = VoucherClient(uniqueSharedId: uniqueId)
        self.client?.delegate = self
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)


        self.client?.startSearching { [unowned self] (authData, displayName, error) -> Void in

            defer {
                self.client?.stop()
            }

            guard let authData = authData, let displayName = displayName else {
                if let error = error {
                    NSLog("Encountered error retrieving data: \(error)")
                }
                self.onNoDataReceived(error as NSError?)
                return
            }

            self.onAuthDataReceived(authData, responderName: displayName)
            
        }
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        self.client?.stop()
    }

    func onNoDataReceived(_ error: NSError?) {
        let alert = UIAlertController(title: "Authentication Failed", message: "The iOS App denied our authentication request.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Bummer!", style: .default, handler: { [unowned self] action in
            self.delegate?.authController(self, didSucceed: false)
            }))
        self.present(alert, animated: true, completion: nil)
    }

    func onAuthDataReceived(_ authData: Data, responderName:String) {
        // Treat the auth data as an string-based auth token
        let tokenString = String(data: authData, encoding: String.Encoding.utf8)!
        let alert = UIAlertController(title: "Received Auth Data!", message: "Received data, '\(tokenString)' from '\(responderName)'", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Awesome!", style: .default, handler: { [unowned self] action in
            self.delegate?.authController(self, didSucceed: true)
            }))
        self.present(alert, animated: true, completion: nil)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func recievedListener(_ service: Array<NetService>) {
        print("SERVCIES \(service)")
        devices = service
        self.deviceTableView.reloadData()
    }

    // MARK: - VoucherClientDelegate

    func voucherClient(_ client: VoucherClient, didUpdateSearching isSearching: Bool) {
        if isSearching {
            self.searchingLabel.text = "📡 Searching for Voucher Servers..\t Enter this code in Android Device \t"+String(randomInt)
        } else {
            self.searchingLabel.text = "❌ Not Searching."
        }
    }

    func voucherClient(_ client: VoucherClient, didUpdateConnectionToServer isConnectedToServer: Bool, serverName: String?) {
        if isConnectedToServer {
            self.connectionLabel.text = "✅ Connected to '\(serverName!)'"
        } else {
            self.connectionLabel.text = "😴 Not Connected Yet."
        }
    }
}

protocol AuthViewControllerDelegate {
    func authController(_ controller:AuthViewController, didSucceed succeeded:Bool)
}


extension AuthViewController: UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return devices?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)
        let device = devices?[indexPath.row]
        
        if((device?.name.hasSuffix("#Android"))!){
            cell.textLabel?.textColor = .blue
        }else{
            cell.textLabel?.textColor = .red
        }
        
        
        let replaceAndroid = device?.name.replacingOccurrences(of: "#Android", with: "")
        let realDeviceName = replaceAndroid?.replacingOccurrences(of: "#IOS", with: "")
        
        cell.textLabel?.text = realDeviceName
        return cell
    }
    
    
}

extension AuthViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let device = devices?[indexPath.row]
        print("You choosen \(device?.name)")
        self.client?.connect(toServer: device!)
    }
    
}

